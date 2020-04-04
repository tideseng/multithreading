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
 * 任何对象都可以实现锁：
 *      Java中的所有对象都派生自Object类，每个实例对象在JVM都对应一个继承自OopDesc的InstanceOopDesc对象，在OopDesc中记录了对象和锁的信息
 *      线程在获取锁时，实际上时获得monitor监视器对象（是一个同步对象）。多个线程访问同步方法/代码块时，相当于去争抢监视器并修改对象中的锁标识
 * 锁状态：
 *      无锁
 *          记录了对象的HashCode、非偏向锁标识
 *      偏向锁--CAS乐观锁（单个线程执行）
 *          记录了线程ID、偏向锁标识
 *          基本原理
 *              当线程访问同步代码块时，在对象头中存储当前线程ID，后续再次进入或退出时直接比较线程ID，相等表示偏向锁是偏向于当前线程，不需要加锁和释放锁
 *          偏向锁获取
 *              先获取锁对象的MarWord、判断是否处于可偏向状态（ThreadId为空、biased_lock=1）
 *              如果是可偏向状态，通过CAS操作将当前线程ID写入MarkWord
 *                  如果CAS成功，表示已获得锁对象的偏向锁，执行同步代码块
 *                  如果CAS失败，表示其它线程已获得偏向锁，则获得偏向锁的线程需要撤销偏向锁升级到轻量级锁
 *              如果是已偏向状态，检查MarkWord中村粗的ThreadID是否等于当前线程的ThreadId
 *                  如果相等，直接执行同步代码块
 *                  如果不相等，说明当前锁偏向于其他线程，获得偏向锁的线程需要撤销偏向锁并升级到轻量级锁
 *          偏向锁撤销
 *              对持有偏向锁的线程进行撤销时，原获得偏向锁的线程有两种情况
 *                  获得偏向锁的线程已执行完同步代码块，会把对象头设置成无锁状态并且其它争抢锁的线程可以基于CAS重新偏向当前线程
 *                  获得偏向锁的线程未执行完同步代码块，会把原获得偏向锁的线程升级为轻量级锁后继续执行同步代码块
 *          说明
 *              实际应用开发中，绝大部分情况下一定会存在多个线程竞争，如果开启偏向锁反而会提升获取锁的资源消耗，可以通过jvm参数UseBiasedLocking来设置开启或关闭偏向锁
 *      轻量级锁--自旋（多个线程交替执行）
 *          记录了指向栈中锁记录的指针
 *          基本原理
 *              当线程竞争锁时，该线程会循环等待而不是阻塞该线程，直到获得锁的线程释放锁（适用于同步代码块执行很快的场景）
 *          轻量级锁加锁
 *              线程在自己的栈中创建锁记录LockRecord
 *              将锁对象的MarkWord复制到锁记录中
 *              将锁记录中的Owner指针指向锁对象
 *              将锁对象的MarkWord替换成指向锁记录的指针
 *          轻量级锁解锁
 *              轻量级锁的锁释放逻辑就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中
 * 	                如果成功表示没有竞争
 * 	                如果失败，表示当前锁存在竞争，那么轻量级锁就会膨胀成为重量级锁
 *          说明
 *              自旋锁默认自旋次数是10次，可通过preBlockSpin修改
 *               JDK1.6后，引入了自适应自旋锁，自适应意味着自旋的次数不是固定不变的，而是根据前一次在同一个锁上自旋的时间以及锁的拥有者的状态来决定
 *      重量级锁--阻塞（多个线程混合执行）
 *          记录了指向重量级锁的指针
 *          基本原理
 *              通过monitorenter指令获取对象监视器(互斥获取)
 *                  执行成功，获取对象锁，执行同步代码块
 *                  执行失败，放入阻塞的同步队列中(线程状态变为BLOCKED)
 *              当获得对象锁的线程执行monitorexit指令释放对象监视器时，会根据算法唤醒阻塞在同步队列的线程尝试获得对象监视器
 *          说明
 *              每一个JAVA对象都会与一个监视器monitor关联，当一个线程执行同步代码块时，该线程得先获取到synchronized修饰的对象对应的monitor
 *              monitor依赖操作系统的MutexLock(互斥锁)来实现的, 线程被阻塞后便进入内核（Linux）调度状态，这会导致系统在用户态与内核态之间来回切换，严重影响锁的性能
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
