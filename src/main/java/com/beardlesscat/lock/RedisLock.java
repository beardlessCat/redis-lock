package com.beardlesscat.lock;

import com.beardlesscat.client.ClientConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RedisLock extends AbstractLock{
    private final String LUA_LOCK_SCRIPT =
            "if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then " +
            "redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";
    private static final String PREFIX = "expire_lock_thread-" ;
    private final AtomicLong THREAD_ID_INDEX = new AtomicLong(0);

    public RedisLock(ClientConfig config) {
        super(config);
    }
    //定时任务
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
        long startMillis = System.currentTimeMillis();
        do {
            if(this.redisLock(name,expire) == 1L){
                log.info("线程{} lock success !",Thread.currentThread().getName());
                //增加锁续签
                this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        scheduleExpiration(name,expire);
                    }
                }, expire/3, expire/3, TimeUnit.SECONDS);
                return;
            }else {
                try {
                    TimeUnit.MILLISECONDS.sleep(BLOCK_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }while (System.currentTimeMillis()-startMillis<timeOut);
        log.warn("线程{}加锁超时失败。",Thread.currentThread().getName());
    }

    /**
     * 锁续签
     * @param name
     */
    private void scheduleExpiration(String name,long expire) {
        Long ttl = jedis.ttl(name);
        if(ttl>0){
            jedis.expire(name,expire) ;
            log.info("线程{}续签锁（{}）成功",Thread.currentThread().getName());
        }else {
            scheduledExecutorService.shutdownNow();
            log.info("线程池停止");
        }
    }

    /**
     * 原子加锁
     * @param name
     * @param expire
     * @return
     */
    private long redisLock(String name,long expire){
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        keys.add(name);
        values.add(LOCK_VALUE);
        values.add(String.valueOf(expire));
        long eval = (long) jedis.eval(LUA_LOCK_SCRIPT, keys, values);
        return  eval;
    }
}
