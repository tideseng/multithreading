package com.tideseng.multithreading.sync;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通过线程同步解决多线程安全问题
 * 目的：
 *      保证只有一个线程在同步中操作共享数据
 * 同步方式：
 *      1.同步方法
 *      2.同步代码块
 *      3.lock锁/同步锁
 * 原理：
 *      多个线程修改同一个资源（全局变量或静态冰凉）
 */
public class ThreadSync {

    private static ThreadSync instance;

    private static Lock lock = new ReentrantLock();

    /**
     * 同步代码块
     * @return
     */
    public static ThreadSync getInstanceBySyncBlock() {
        if (instance == null) { // 外层判断是为了避免每次都执行同步代码块
            // 当一个线程抢夺CPU资源拿到锁对象后，其它抢夺到CPU资源准备获取锁对象的线程进入栈中等待(阻塞状态)，等到锁对象被释放后，先进后出依次获取锁对象执行同步代码
            synchronized (ThreadSync.class) { // 获取锁对象，对该区块的资源实行互斥访问（锁对象在多线程间要保证是同一个）
                if (instance == null)
                    instance = new ThreadSync();
            }
        }
        return instance;
    }

    /**
     * 同步方法（静态方法的锁对象是类的class对象、非静态方法的锁对象是类的实例对象）
     * @return
     */
    public synchronized static ThreadSync getInstanceBySyncMethod() {
        if(instance == null)
            instance = new ThreadSync();
        return instance;
    }

    /**
     * lock锁/同步锁，在获取锁和释放锁之间执行同步逻辑
     * @return
     */
    public static ThreadSync getInstanceBySyncLock() {
        if (instance == null) {
            lock.lock(); // 加同步锁
            if (instance == null)
                instance = new ThreadSync();
            lock.unlock(); // 释放同步锁
        }
        return instance;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                ThreadSync instance = ThreadSync.getInstanceBySyncBlock(); // 两个线程并发运行，开始不断抢夺CPU执行权去创建实例
                System.out.println(instance);
            }).start();
        }
    }

}
