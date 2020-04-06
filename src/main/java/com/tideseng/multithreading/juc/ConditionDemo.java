package com.tideseng.multithreading.juc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionDemo {

    public static void main(String[] args) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        new Thread(new ConditionWait(lock, condition)).start();
        new Thread(new ConditionSignal(lock, condition)).start();
    }

    static class ConditionWait implements Runnable{
        private Lock lock;
        private Condition condition;
        public ConditionWait(Lock lock, Condition condition){
            this.lock=lock;
            this.condition=condition;
        }
        @Override
        public void run() {
            System.out.println("begin - ConditionWait");
            try {
                lock.lock();
                condition.await();
                System.out.println("end - ConditionWait");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }

    static class ConditionSignal implements Runnable{
        private Lock lock;
        private Condition condition;
        public ConditionSignal(Lock lock, Condition condition){
            this.lock=lock;
            this.condition=condition;
        }
        @Override
        public void run() {
            System.out.println("begin - ConditionSignal");
            try {
                lock.lock();
                condition.signal();
                System.out.println("end - ConditionSignal");
            }finally {
                lock.unlock();
            }
        }
    }

}
