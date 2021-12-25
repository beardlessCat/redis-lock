package com.beardlesscat.lock;

import com.beardlesscat.client.ClientConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.UUID;

@Slf4j
public abstract class AbstractLock implements Lock{
    /**
     * 锁默认存活时间（秒）
     */
    protected static final long TIME = 3;

    /**
     * 获取锁失败阻塞时间
     */
    protected static final long BLOCK_WAIT_TIME = 1*1000;

    /**
     * 获取锁失败超时时间（默认是一直等待）
     */
    protected static final long WAIT_TIME_OUT = -1;

    protected static String uid ;

    protected Jedis jedis ;

    public AbstractLock(ClientConfig config) {
        jedis = new Jedis(config.getHost(),config.getPort());
        uid = UUID.randomUUID().toString().replace("-","") ;
    }

    @Override
    public abstract void unlock(String name) ;
}
