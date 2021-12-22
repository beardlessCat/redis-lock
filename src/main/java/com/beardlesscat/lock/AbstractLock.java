package com.beardlesscat.lock;

import com.beardlesscat.client.ClientConfig;
import redis.clients.jedis.Jedis;

public abstract class AbstractLock implements Lock{
    /**
     * 锁默认存活时间
     */
    protected static final long TIME = 3*1000;
    /**
     * 锁的value值
     */
    protected static final String LOCK_VALUE = "1";
    /**
     * 获取锁失败阻塞时间
     */
    protected static final long BLOCK_WAIT_TIME = 1*1000;

    /**
     * 获取锁失败超时时间（默认是一直等待）
     */
    protected static final long WAIT_TIME_OUT = -1;

    protected Jedis jedis ;

    public AbstractLock(ClientConfig config) {
        jedis = new Jedis(config.getHost(),config.getPort());
    }
}