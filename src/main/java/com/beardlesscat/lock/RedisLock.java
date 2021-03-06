package com.beardlesscat.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RedisLock extends AbstractLock{
    private final String LUA_LOCK_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 0) then                   \n" +
            "    redis.call('hset', KEYS[1], ARGV[2], 1);                   \n" +
            "    redis.call('expire', KEYS[1], ARGV[1]);                    \n" +
            "    return 1;                                                  \n" +
            "end;                                                           \n" +
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then         \n" +
            "    redis.call('hincrby', KEYS[1], ARGV[2], 1);                \n" +
            "    redis.call('expire', KEYS[1], ARGV[1]);                    \n" +
            "    return 2;                                                  \n" +
            "end;                                                           \n" +
            "return 0;                                                        ";

    private static final String LUA_UNLOCK_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 0) then                   \n" +
            "    return 1;                                                  \n" +
            "end;                                                           \n" +
            "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then         \n" +
            "    return 2;                                                  \n" +
            "end;                                                           \n" +
            "local counter = redis.call('hincrby', KEYS[1], ARGV[1], -1);   \n" +
            "if (counter > 0) then                                          \n" +
            "    return 3;                                                  \n" +
            "else                                                           \n" +
            "    redis.call('del', KEYS[1]);                                \n" +
            "    return 0;                                                  \n" +
            "end;                                                           \n" +
            "return nil;                                                    \n";

    ThreadLocal<Long> time = new ThreadLocal<>();
    private static final String PREFIX = "expire_lock_thread-" ;
    private final AtomicLong THREAD_ID_INDEX = new AtomicLong(0);

    private volatile AtomicBoolean isExpireThreadRunning = new AtomicBoolean(false);
    private AtomicInteger reinCount = new AtomicInteger(0);

    public RedisLock() {
        super();
    }

    //????????????
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor((r)->{
        Thread thread = new Thread(r, PREFIX + this.THREAD_ID_INDEX.incrementAndGet());
        thread.setDaemon(false);
        return thread;
    });

    @Override
    public void lock(String name) {
        this.lock(name,TIME);
    }

    @Override
    public void lock(String name, long expire) {
        this.lock(name,TIME,WAIT_TIME_OUT);
    }

    @Override
    public void lock(String name, long expire, long timeOut) {
        time.set(System.currentTimeMillis());
        String value = this.getThreadId();
        do {
            long state = this.redisLock(name, value, expire);
            if( state== 1L){
                log.info("??????{} lock success !",Thread.currentThread().getName());
                //???????????????
                if(!isExpireThreadRunning.get()){
                    isExpireThreadRunning.compareAndSet(false,true);
                    this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            scheduleExpiration(name,expire);
                        }
                    }, expire/3, expire/3, TimeUnit.SECONDS);
                }
                return;
            }else if(state== 2L){
                log.info("??????{} lock success,???????????????{} !",Thread.currentThread().getName(),reinCount.incrementAndGet());
                return;
            }else {
                try {
                    TimeUnit.MILLISECONDS.sleep(BLOCK_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }while ((System.currentTimeMillis()-time.get())<timeOut);
        log.warn("??????{}?????????????????????",Thread.currentThread().getName());
        throw new RuntimeException("??????????????????");
    }

    /**
     * ??????????????????id???uuid+threadId???
     * @return
     */
    private String getThreadId() {
        String threadId = uid + ":" + Thread.currentThread().getId();

        log.info("{}:threadId:{}",Thread.currentThread().getName(),threadId);
        return threadId;
    }

    /**
     * ?????????
     * @param name
     */
    private void scheduleExpiration(String name,long expire) {
        Long ttl = jedis.ttl(name);
        if(ttl>0){
            jedis.expire(name,expire) ;
            log.info("???????????????",Thread.currentThread().getName());
        }else {
            scheduledExecutorService.shutdownNow();
            isExpireThreadRunning.compareAndSet(false,true);
            log.info("???????????????");
        }
    }

    /**
     * ????????????
     * @param name
     * @param expire
     * @return
     */
    private long redisLock(String name,String value ,long expire){
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        keys.add(name);
        values.add(String.valueOf(expire));
        values.add(value);
        long eval = (long) jedis.eval(LUA_LOCK_SCRIPT, keys, values);
        if(eval==1L){
            log.warn("??????{}????????????{}:{}",Thread.currentThread().getName(),name,value);
        }
        return  eval;
    }

    /**
     * ??????
     * @param name
     */
    @Override
    public void unlock(String name) {
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        keys.add(name);
        String threadId = this.getThreadId();
        values.add(threadId);
        log.warn("{}:???????????????{}:{}",Thread.currentThread().getName(),name,threadId);
        long eval = (long) jedis.eval(LUA_UNLOCK_SCRIPT, keys, values);
        log.info("??????{}????????????????????????{}",Thread.currentThread().getName(),eval);
        if(eval==0){
            scheduledExecutorService.shutdownNow();
            log.info("????????????????????????????????????");
        }else if(eval == 1||eval == 2 ) {
            log.warn("?????????????????????{}:{}",name,threadId);
        }
    }
}
