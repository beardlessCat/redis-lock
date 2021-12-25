package com.beardlesscat;

import com.beardlesscat.client.ClientConfig;
import com.beardlesscat.lock.Lock;
import com.beardlesscat.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
@Slf4j
public class LockTest {
    @Test
    public void lock() throws InterruptedException {
        ClientConfig config = new ClientConfig("demo.redis.cn",16379);
        Lock lock = new RedisLock(config);
        try {
            lock.lock("lock");
            lock.lock("lock");
            TimeUnit.SECONDS.sleep(5);
            lock.unlock("lock");
            TimeUnit.SECONDS.sleep(5);
            lock.unlock("lock");
        } finally {
            log.info("线程解锁成功");
        }
        TimeUnit.SECONDS.sleep(2*60);
        log.info("JVM即将退出");
    }
}
