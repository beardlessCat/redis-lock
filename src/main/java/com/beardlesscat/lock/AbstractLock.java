package com.beardlesscat.lock;

import com.beardlesscat.client.JedisPoolManager;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.util.UUID;

@Slf4j
public abstract class AbstractLock implements Lock{
    /**
     * 锁默认存活时间（秒）
     */
    protected static final long TIME = 300;

    /**
     * 获取锁失败阻塞时间
     */
    protected static final long BLOCK_WAIT_TIME = 1*1000;

    /**
     * 获取锁失败超时时间（默认是一直等待）
     */
    protected static final long WAIT_TIME_OUT = Long.MAX_VALUE;

    protected static String uid ;
    protected Jedis jedis ;

    public AbstractLock() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1024);
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setTestOnReturn(true);
        uid = UUID.randomUUID().toString().replace("-","");
        JedisPoolManager instance = JedisPoolManager.getInstance();
        Jedis resource = instance.getJedisPool().getResource();
        this.jedis = resource;
    }
    @Override
    public abstract void unlock(String name) ;
}
