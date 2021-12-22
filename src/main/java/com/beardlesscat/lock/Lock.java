package com.beardlesscat.lock;

public interface Lock {
    /**
     * 加锁
     * @param name
     */
    void lock(String name);

    /**
     * 加锁
     * @param name
     */
    void lock(String name,long expire);

    /**
     * 加锁
     * @param name
     */
    void lock(String name,long expire,long timeOut);
    /**
     * 解锁
     * @param name
     */
    void unlock(String name);
}
