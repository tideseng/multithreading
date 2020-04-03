package com.tideseng.multithreading.sync;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通过线程同步解决多线程安全问题
 * 目的：
 *      保证只有一个线程在同步中操作共享数据，即保证数据安全性的同时还要保证性能
 * 同步方式：
 *      1.synchronized同步方法
 *      2.synchronized同步代码块
 *      3.lock锁/同步锁
 * 锁对象：
 *      对象锁
 *      类锁
 * 锁的特点：
 *      共享
 *      互斥
 * 对象在内存中的存储布局分为三个区域：
 *      对象头MarkWord（存储了对象和锁的信息）
 *          hashcode
 *          分代年龄
 *          同步锁标记
 *          偏向锁标记
 *          偏向锁持有者线程id
 *          monitor()（ObjectMonitor争抢锁的实现逻辑）
 *      实例数据Instance Data
 *      对齐填充Padding
 * 锁状态：
 *      无锁
 *          记录了对象的HashCode、非偏向锁
 *      偏向锁--CAS乐观锁（单个线程执行）
 *          记录了线程ID、是偏向锁
 *      轻量级锁--自旋（多个线程交替执行）
 *          记录了指向栈中锁记录的指针
 *      重量级锁--阻塞（多个线程混合执行）
 *          记录了指向重量级锁的执行
 *      GC标记
 *          空
 * 说明：
 *      JDK1.6之前，基于重量级锁(synchronized)来实现
 *      JDK1.6中为了减少获得锁和释放锁带来的性能消耗而引入的偏向锁和轻量级锁
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
