package com.beardlesscat.client;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolManager {

    private static final String HOST="demo.redis.cn";
    private static final int PORT =16379;
    private static final int TIME_OUT = 2000;
    private JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    private volatile static JedisPoolManager manager;
    private JedisPoolManager (){}
    public static JedisPoolManager getInstance() {
        if (manager == null) {
            synchronized (JedisPoolManager.class) {
                if (manager == null) {
                    manager = new JedisPoolManager();
                    manager.setJedisPool(manager.initJedisPool()) ;
                }
            }
        }
        return manager;
    }
    private JedisPool initJedisPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1024);
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setTestOnReturn(true);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,HOST, PORT, TIME_OUT);
        return jedisPool;
    }
}
