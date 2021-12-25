package com.beardlesscat;

import com.beardlesscat.lock.Lock;
import com.beardlesscat.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class LockTest {
    @Test
    public void lock() throws InterruptedException {
        new Thread(()->{
            try {
                asSale();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"other-thread-0").start();
        new Thread(()->{
            try {
                asSale();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"other-thread-2").start();

        TimeUnit.SECONDS.sleep(5);
        asSaleReLock();
        TimeUnit.SECONDS.sleep(50);

    }

    private void asSale() throws InterruptedException {
        Lock lock = new RedisLock();
        try {
            lock.lock("lock");
            TimeUnit.SECONDS.sleep(5);
        } finally {
            lock.unlock("lock");
            log.info("线程解锁成功");
        }
    }

    private void asSaleReLock() throws InterruptedException {
        Lock lock = new RedisLock();
        try {
            lock.lock("lock");
            TimeUnit.SECONDS.sleep(3);
            lock.lock("lock");
            TimeUnit.SECONDS.sleep(3);
            lock.unlock("lock");
            TimeUnit.SECONDS.sleep(3);
            lock.unlock("lock");
        } finally {
            log.info("线程解锁成功");
        }
    }
}
