package com.tideseng.multithreading.juc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 重入锁
 */
public class ReentrantDemo {

    /**
     * synchronized关键字实现重入
     */
    public synchronized void syncReentrant1(){
        System.out.println("syncReentrant1--"+Thread.currentThread().getName());
        syncReentrant2();
    }
    private void syncReentrant2(){
        synchronized (this){
            System.out.println("syncReentrant2--"+Thread.currentThread().getName());
        }
    }

    /**
     * ReentrantLock实现重入
     */
    private Lock lock = new ReentrantLock();
    public void lockReentrant1(){
        lock.lock(); // 获得锁
        System.out.println("lockReentrant1--"+Thread.currentThread().getName());
        lockReentrant2();
        lock.unlock(); // 释放锁
    }
    private void lockReentrant2(){
        lock.lock();
        System.out.println("lockReentrant2--"+Thread.currentThread().getName());
        lock.unlock();
    }

    public static void main(String[] args) {
        ReentrantDemo threadReentrant = new ReentrantDemo();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> threadReentrant.syncReentrant1()).start();
            new Thread(() -> threadReentrant.lockReentrant1()).start();
        }
    }

}
