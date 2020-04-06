package com.tideseng.multithreading.juc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 重入读写锁，适用读多写少的场景
 */
public class ReentrantReadWriteDemo {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    public Lock read = lock.readLock();
    public Lock write = lock.writeLock();
    public Map<String, Object> redis = new HashMap<>();

    /**
     * 读线程和读线程不互斥，所以会出现多个读线程同时输出打印语句
     * @param key
     * @return
     */
    public Object get(String key){
        read.lock();
        System.out.println(Thread.currentThread().getName()+"--读取数据");
        try{
            return redis.get(key);
        } finally {
            read.unlock();
        }
    }

    /**
     * 读线程和写线程互斥、写线程和写线程互斥
     * @param key
     * @param value
     * @return
     */
    public Object put(String key, Object value){
        write.lock();
        System.out.println(Thread.currentThread().getName()+"--写入数据");
        try{
            return redis.put(key, value);
        } finally {
            write.unlock();
        }
    }

    public static void main(String[] args) {
        ReentrantReadWriteDemo readWrite = new ReentrantReadWriteDemo();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                if(Thread.currentThread().getName().contains("2"))
                    readWrite.put("name", "佳欢");
                else
                    System.out.println(readWrite.get("name"));
            }).start();
        }
    }

}
