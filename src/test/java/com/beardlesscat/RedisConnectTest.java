package com.beardlesscat;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
public class RedisConnectTest {
    @Test
    public void connectTest(){
        Jedis jedis = new Jedis("demo.redis.cn",16379);
        System.out.println("连接成功");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
        System.out.println("======================key==========================");
        //设置键值对
        jedis.set("a","123");
        //查看存储的键的总数
        System.out.println(jedis.dbSize());
        //取出设置的键值对并打印
        System.out.println(jedis.get("a"));
    }
}
