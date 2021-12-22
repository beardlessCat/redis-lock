package com.beardlesscat;

import com.beardlesscat.client.ClientConfig;
import com.beardlesscat.lock.Lock;
import com.beardlesscat.lock.RedisLock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class LockTest {
    @Test
    public void lock() {
        ClientConfig config = new ClientConfig("demo.redis.cn",16379);
        Lock lock = new RedisLock(config);
        try {
            lock.lock("lock");
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock("lock");
        }
    }
}
