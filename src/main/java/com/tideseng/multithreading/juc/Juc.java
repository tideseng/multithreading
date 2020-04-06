package com.tideseng.multithreading.juc;

/**
 * java.util.concurrent并发工具包
 *
 * 包含很多用来在并发场景中使用的组件，如线程池、阻塞队列、计时器、同步器、并发集合等等
 *
 * Lock
 *      Lock简介
 *          在Lock接口出现之前，多线程的并发安全处理只能基于synchronized关键字解决（但并不适合于所有的并发场景）
 *          Java5以后，Lock解决了synchronized在某些场景中的短板，且比synchronized更加灵活
 *      接口方法
 *          void lock() // 如果锁可用就获得锁，如果锁不可用就阻塞直到锁释放
 *          void lockInterruptibly() // 和lock()方法相似, 当阻塞的线程中断时抛出java.lang.InterruptedException异常
 *          boolean tryLock() // 非阻塞获取锁;尝试获取锁，如果成功返回true
 *          boolean tryLock(long timeout, TimeUnit timeUnit) // 带有超时时间的获取锁方法
 *          void unlock() // 释放锁
 *      Lock实现
 *          Lock接口定义了释放锁和获得锁的标准规范，不同的实现类意味着锁的不同实现
 *      常见锁实现
 *          ReentrantLock
 *              重入锁，它是唯一一个实现了Lock接口的类
 *              重入锁指线程在获得锁之后，再次获取该锁不需要阻塞，而是直接关联一次计数器增加重入次数（避免线程死锁）
 *          ReentrantReadWriteLock
 *              重入读写锁，实现了ReadWriteLock接口，在这个类中维护了两个锁：ReadLock、WriteLock，分别实现了Lock接口
 *              读写锁是一种适合读多写少的场景下解决线程安全问题的工具，基本原则是：读和读不互斥、读和写互斥、写和写互斥，即涉及到影响数据变化的操作都会存在互斥
 *          StampedLock
 *              StampedLock是一种乐观的读策略（JDK8引入的新的锁机制，是读写锁的一个改进版本）
 *              读写锁中读和写有冲突，如果大量的读线程存在可能会引起写线程的饥饿，StampedLock使得乐观锁完全不会阻塞写线
 * AQS
 *      概念
 *          Lock中有一个同步队列AQS(AbstractQueuedSynchronizer)，它是一个同步工具也是Lock用来实现线程同步的核心组件
 *          实现了线程的阻塞以及唤醒，但它并不具备业务功能， 以在不同的同步场景中会继承AQS来实现对应场景的功能
 *      功能
 *          独占
 *              独占锁，每次只能有一个线程持有锁（如ReentrantLock）
 *          共享
 *              共享锁，允许多个线程同时获取锁，并发访问共享资源（如ReentrantReadWriteLock）
 *      内部实现
 *          AQS队列内部维护了一个FIFO的双向链表(head、tail)，每个数据节点Node都有两个指针(prev前驱节点和next后继节点)
 *          双向链表可以从任意一个节点Node开始很方便的访问前驱和后继
 *          当线程争抢锁失败后会封装到Node并放入ASQ队列中去
 *          当获取锁的线程释放锁后，会从队列中唤醒一个阻塞的节点(线程)
 *      结构
 *          Node
 *              组成
 *                  volatile Node prev; // 前驱节点
 *                  volatile Node next; // 后继节点
 *                  volatile Thread thread; // 当前线程
 *                  Node nextWaiter; // 存储在Condition队列中的后继节点
 *              状态
 *                  CANCELLED(1)
 *                      在同步队列中等待的线程等待超时或被中断，需要从同步队列中取消该Node的结点
 *                  SIGNAL(-1)
 *                      只要前置节点释放锁，就会通知标识为SIGNAL状态的后续节点的线程
 *                  CONDITION(-2)
 *                      和Condition有关系
 *                  PROPAGATE(-3)
 *                      共享模式下，PROPAGATE状态的线程处于可运行状态
 *                  默认状态(0)
 *                      初始状态
 *              head节点
 *                  head节点表示获取锁成功的节点，当头结点在释放同步状态时，会唤醒后继节点
 *                  如果后继节点获得锁成功，会把自己设置为头结点
 *              添加节点
 *                  1. 新的线程封装成Node节点追加到同步队列，将prev节点指向上一个Node、上一个Node的next节点指向自己
 *                  2. 通过CAS将tail重新指向新的尾部节点
 *              释放节点
 *                  1. 将head节点重新指向获得锁的后继节点、断开原head节点的next引用
 *                      设置head节点不需要通过CAS，由获得锁的节点线程来设置head节点
 *                  2. 将新获得锁的节点的prev节点指向null
 *          ConditionObject
 * ReentrantLock
 *      以独占方式实现的互斥锁，避免线程的死锁
 *      时序图
 * 		    ReentrantLock			NofairSync			Sync			AbstractQueuedSynchronizer
 *
 * 		          ----------------lock()----------------->
 *
 * 		                               <------lock()-----
 *
 * 		    							------if------compareAndSetState()----------->
 *
 * 			    						------else-------------acquire()------------->
 *
 * 			    						<-------------------tryAcquire()--------------
 *
 * 		                               -nonfairTryAcquire()->
 *
 * 	    	                           <---true/false----
 *
 * 		    							--------------------false-------------------->
 *
 *  																				  ------
 *  																						|
 *  																						| addWaiter()
 *  																						|
 *  																				  <-----
 *      源码分析
 *          Sync.lock()是ReentrantLock获取锁的入口
 *              Sync是一个首相的静态内部类，继承自AQS实现了重入锁的逻辑
 *              Sync 有两个具体的实现类
 *                  NofairSync
 *                      表示可以存在抢占锁的功能，即不管当前队列上是否存在其他线程等待，新线程都会通过CAS尝试抢占（插队）
 *                  FailSync
 *                      表示所有线程严格按照FIFO来获取锁
 *          NofairSync.lock()
 *              CAS成功，就表示成功获得了锁(compareAndSetState(0, 1))
 *                  通过CAS乐观锁的方式来左比较并替换
 *                  当前内存中的state的值和预期值expect相等，则替换为update；更新成功返回true，否则返回false
 *                      Unsafe.getUnsafe().compareAndSwapInt(this, stateOffset, expect, update);
 *                          Unsafe类是在sun.misc包下，不属于Java标准
 *                          但很多Java的基础类库，包括一些被广泛使用的高性能开发库都是基于Unsafe类开发的，比如Netty、Hadoop、Kafka等
 *                          Unsafe提供了一些低层次操作，如直接内存访问、线程的挂起和恢复、CAS、线程同步、内存屏障
 *                          CAS就是Unsafe类中提供的一个原子操作，整个方法的作用是如果当前时刻的值等于预期值相等，则更新为新的期望值
 * 	                            第一个参数为需要改变的对象
 * 	                            第二个为偏移量(stateOffset)
 * 	                                stateOffset表示state这个字段在AQS类的内存中相对于该类首地址的偏移量
 * 	                            第三个参数为预期值
 * 	                            第四个为更新值
 * 	                            如果更新成功，则返回true，否则返回false
 *                  CAS操作是原子的，不会出现线程安全问题，这里涉及到Unsafe这个类的操作，以及涉及到state这个属性的意义
 *                  state是AQS中的一个属性，它在不同的实现中所表达的含义不一样，对于重入锁的实现来说，表示一个同步状态
 *                      当 state=0 时，表示无锁状态
 *                      当 state>0 时，表示已经有线程获得了锁的次数
 *              CAS失败，调用acquire(1)走锁竞争逻辑(compareAndSetState(0, 1))
 *          AQS.acquire(1)
 *              tryAcquire(arg)
 *                  通过tryAcquire尝试获取独占锁，如果成功返回true，失败返回false
 *                      NonfairSync.nofairTryAcquire(1)
 *                          获取当前线程、当前的锁的状态
 *                          如果state=0处于无锁状态，通过cas更新state状态的值（插队）
 *                          如果当前线程是属于重入，则增加重入次数
 *                      FairSync.tryAcquire(1)
 *                          获取当前线程、当前的锁的状态
 *                          如果state=0处于无锁状态且队列为空时，通过cas更新state状态的值（不插队）
 *                          如果当前线程是属于重入，则增加重入次数
 *              如果tryAcquire失败，则会通过addWaiter()方法将当前线程封装成Node添加到AQS队列尾部
 *              调用addWaiter()方法后再调用acquireQueued()，将Node作为参数，通过自旋去尝试获取锁
 *          AQS.addWaiter()
 *              传递Node.EXCLUSIVE的入参mode参数
 *                  表示当前节点的状态为独占状态、意味着重入锁用到了AQS的独占锁功能
 *              将当前线程封装到Node
 *              判断当前链表中的tail节点是否为空
 *                  如果不为空说明队列已存在节点(包括head和tail)
 *                      将当前Node的前驱节点指向tail、通过cas把当前线程的node添加到AQS队列、原tail节点的后继节点指向当前Node
 *              如果当前链表中的tail节点为空或者cas失败，调用enq()方法自旋将节点添加到AQS队列
 *                  如果当前链表中的tail节点为空，通过cas初始化head节点、tail节点也指向head节点
 *                  如果当前链表中的tail节点不为空，当前Node的前驱节点指向tail、通过cas把当前线程的node添加到AQS队列、原tail节点的后继节点指向当前Node
 *          AQS.acquireQueued()
 *              将已经放入链表的node自旋竞争锁
 *              获取当前节点的prev节点
 *              如果prev节点为head节点，那么它就有资格去争抢锁，调用tryAcquire抢占锁
 *              抢占锁成功以后，把获得锁的节点设置为head，并且移除原来的初始化head节点（新head节点的prev=null、原head节点的next=null）
 *              如果获得锁失败，则调用shouldParkAfterFailedAcquire()根据waitStatus决定是否需要调用parkAndCheckInterrupt()挂起线程
 *              最后通过cancelAcquire取消获得锁的操作
 *          AQS.shouldParkAfterFailedAcquire()
 *              作用：通过Node的状态来判断，ThreadA竞争锁失败以后是否应该被挂起
 *              如果 ThreadA 的 pred 节点状态为 SIGNAL，那就表示可以放心挂起当前线程
 *              通过循环扫描链表把 CANCELLED 状态的节点移除
 *              修改 pred 节点的状态为 SIGNAL,返回 false
 *          AQS.parkAndCheckInterrupt()
 *              作用：挂起当前线程使其处于WATING状态
 *              调用LockSupport.park()挂起当前线程
 *                  最终调用的是Unsafe.park()
 *              调用Thread.interrupted()返回当前线程是否被其他线程触发过中断请求
 *
 *          Sync.unlock()是ReentrantLock释放锁的入口
 *          AQS.release(1)
 *              作用：释放锁
 *              调用tryRelease()判断是否释放，当head存在且waitStatus不为0时调用unparkSuccessor()释放锁
 *          ReentrantLock.tryRelease(1)
 *              通过将state状态减掉传入的参数值，如果结果状态为0，就将排它锁的Owner设置为null，以使得其它的线程有机会进行执行
 *              在排它锁中，加锁的时候状态会增加1，在解锁的时候减掉1，只有unlock()的次数与lock()的次数对应才会将Owner线程设置为空返回true
 *          AQS.unparkSuccessor()
 *              获得 head 节点的waitStatus状态
 *              如果waitStatus状态<0，通过cas将状态设置为0
 *              得到 head 节点的下一个节点
 *              如果下一个节点为null或者 status>0 表示 cancelled 状态
 *                  通过从尾部节点开始扫描，找到距离 head 最近的一个waitStatus<=0 的节点
 *              如果下一个节点不为null，直接唤醒线程
 * ReentrantReadWriteLock
 *      在同一时刻可以允许多个线程访问，但是在写线程访问时其他读写线程都会被阻塞
 *      一般情况下，读写锁的性能都会比排它锁好，因为大多数场景读是多于写的，能够提供比排它锁更好的并发性和吞吐量
 */
public class Juc {

    private volatile int state;

    public static void main(String[] args) throws NoSuchFieldException {

    }

}
