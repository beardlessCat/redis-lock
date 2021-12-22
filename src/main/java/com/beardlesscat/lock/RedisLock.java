package com.beardlesscat.lock;

import com.beardlesscat.client.ClientConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
@Slf4j
public class RedisLock extends AbstractLock{

    public RedisLock(ClientConfig config) {
        super(config);
    }

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
            if(jedis.setnx(name,LOCK_VALUE)==1L){
                jedis.expire(name,expire);
                log.info("线程{} lock success !",Thread.currentThread().getName());
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

    @Override
    public void unlock(String name) {
        jedis.del(name);
        log.info("unlock success !");
    }
}
