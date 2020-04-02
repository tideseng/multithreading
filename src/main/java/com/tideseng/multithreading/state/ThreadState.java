package com.tideseng.multithreading.state;

/**
 * 线程状态
 *      NEW（新建）：线程创建后启动前(new之后start之前)的状态
 *      RUNNABLE（可运行）：可运行线程代码的状态(是否正在运行取决于是否抢占到CPU的执行权)
 *      BLOCKED（阻塞）：正在等待监视器锁/对象锁或调用wait后再次进入同步代码块/放的的状态(当锁对象被释放并被该线程持有时变成Runnable状态)
 *          进入synchronized代码块/方法时获取不到监视器锁而阻塞
 *          已在synchronized代码块/方法中且被唤醒但获取不到监视器锁而阻塞
 *      WAITING（无限等待）：无期限的等待被其它线程唤醒的状态
 *          不带超时值的Object.wait(): 让出锁，并进入对象的等待队列。其它线程调用notify/notifyAll时恢复Runnable状态
 *          不带超时值的Thread.join(): 等待指定线程终止
 *          LockSupport.park(): 通过unpark唤醒
 *      TIMED_WAITING（计时等待）：有期限的等待(sleep)线程被唤醒的状态
 *          sleep(): 不让出锁的有限等待，时间到期后自动恢复Runnable状态。等待时间并不是线程暂停运行的最短时间，不能保证到期后就开始立刻执行
 *          带有超时值的Object.wait(): 时间到期后自动恢复Runnable状态
 *          带有超时值的Thread.join()
 *          LockSupport.parkNanos、LockSupport.parkUntil
 *      TERMINATED（终止）：正常退出run()方法或异常终止run方法的状态
 * IDEA的Threads窗口状态
 *      RUNNING--RUNNABLE、WAIT--WAITING/TIMED_WAITING、MONITOR--BLOCKED
 */
public class ThreadState {

    private Object object = new Object();

    /**
     * 查看线程的NEW、RUNNABLE、TERMINATED状态
     * @throws InterruptedException
     */
    public void runnable() throws InterruptedException {
        Thread thread = new Thread(){
            @Override
            public void run() {
                System.out.println("子线程状态：" + Thread.currentThread().getState()); // RUNNABLE
            }
        };
        System.out.println("当前子线程状态"+thread.getState()); // NEW
        System.out.println("当前主线程状态：" + Thread.currentThread().getState()); // RUNNABLE

        thread.start();

        // 手动Debug或添加睡眠时间比较容易让子线程成为TERMINATED状态
        Thread.sleep(1000);
        System.out.println("当前子线程状态："+thread.getState()); // RUNNABLE(run方法未结束)/TERMINATED(run方法已结束)
        System.out.println("当前主线程状态：" + Thread.currentThread().getState()); // RUNNABLE
    }

    /**
     * 查看线程的BLOCKED状态
     *  1.手动Debug让两个线程先后进入同步代码块;
     *  2.在idea控制台输入:jps;
     *  3.找出当前类的pid;
     *  4.在idea控制台输入:jstack pid;
     *  5.查看指定线程名的状态（结果一个为RUNNABLE、另一个为BLOCKED(on object monitor|waiting for monitor entry)）
     */
    private static void blocked1(){
        ThreadState object = new ThreadState();
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                object.run();
            }, "佳欢"+i).start();
        }
    }
    private void run(){
        synchronized (this){
            System.out.println(Thread.currentThread().getName()); // 在这里打断点，让两个线程先后进入同步代码块
        }
    }

    /**
     * 查看线程的BLOCKED、WAITING状态
     *      佳欢0/1先后进入同步方法，此时佳欢0处于RUNNABLE状态、佳欢1处于BLOCKED状态(on object monitor|waiting for monitor entry) -- 这里涉及到EntrySet
     *      佳欢0执行wait()方法，此时佳欢0处于WAITING状态(on object monitor|in Object.wait())、佳欢1处于Runnable状态
     *      佳欢1执行wait()方法，此时佳欢0/1处于WAITING状态(on object monitor|in Object.wait()) -- 这里设计到Wait Set，先进后出所以佳欢1先唤醒
     *      佳欢2进入同步方法，此时佳欢0/1处于WAITING状态(on object monitor|in Object.wait())、佳欢2处于RUNNABLE状态
     *      佳欢2执行notifyAll()方法，此时佳欢0/1处于BLOCKED状态(on object monitor|in Object.wait())、佳欢2处于RUNNABLE状态
     *      佳欢2释放锁对象之后，此时佳欢0处于BLOCKED状态(on object monitor|in Object.wait())、佳欢1/2处于RUNNABLE状态
     *      佳欢1释放锁对象之后，此时佳欢0/1/2处于RUNNABLE状态
     * 注意：
     *      当佳欢0/1先后进入同步方法后、佳欢0执行wait()方法前，佳欢2不能进入同步方法，否则会出现佳欢1一直处于waiting状态
     *      如果佳欢0/1/2先后进入同步方法，由于先进后出导致佳欢0执行wait()方法释放锁后，佳欢2拿到锁执行notifyAll()方法，而当佳欢1的wait()执行后没有线程会唤醒它
     *          【可以在wait()方法中设置自动唤醒时间避免这一问题】
     */
    private static void blocked2() throws InterruptedException {
        ThreadState object = new ThreadState();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                if(Thread.currentThread().getName().contains("2"))
                    object.notifyBlocked();
                else
                    object.waitBlocked();
            }, "佳欢"+i).start();
        }
    }
    private void waitBlocked(){
        try {
            synchronized(object) { // 锁对象要与调用调用wait()/notify()/notifyAll()方法的对象保持一致
                System.out.println(Thread.currentThread().getName() + "开始运行");
                object.wait(); // 只有锁对象才能调用wait()/notify()/notifyAll()方法
                System.out.println(Thread.currentThread().getName() + "结束运行");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void notifyBlocked(){
        synchronized (object) {
            System.out.println(Thread.currentThread().getName() + "唤醒所有线程");
            object.notify(); // 只有锁对象才能调用wait()/notify()/notifyAll()方法（notify()会唤醒佳欢0并让佳欢0获取对象锁、notifyAll()会唤醒佳欢0/1并让佳欢1获取对象锁）
            System.out.println(Thread.currentThread().getName() + "唤醒所有线程");
        }
    }

    /**
     * 查看线程的BLOCKED、TIMED_WAITING状态（wait()方法会释放锁）
     *      佳欢0/1先后进入同步方法，此时佳欢0处于RUNNABLE状态、佳欢1处于BLOCKED状态(on object monitor|waiting for monitor entry)
     *      佳欢0执行wait(30000)方法，此时佳欢0处于TIMED_WAITING状态(on object monitor|in Object.wait())、佳欢1处于RUNNABLE状态
     *      佳欢1执行wait(30000)方法，此时佳欢0/1处于TIMED_WAITING状态(on object monitor|in Object.wait())
     *      佳欢0/1超时时间结束之后，此时佳欢0处于RUNNABLE状态、佳欢1处于BLOCKED状态(on object monitor|waiting for monitor entry)
     *      佳欢0释放锁对象之后，此时佳欢1处于RUNNABLE状态
     */
    private static void blocked3() {
        ThreadState object = new ThreadState();
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                object.timeWaitBlocked();
            }, "佳欢"+i).start();
        }
    }
    private synchronized void timeWaitBlocked(){
        try {
            System.out.println(Thread.currentThread().getName()+"开始运行");
            this.wait(30000);
            System.out.println(Thread.currentThread().getName()+"结束运行");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看线程的BLOCKED、TIMED_WAITING状态（sleep()方法不会释放锁）
     *      佳欢0/1先后进入同步方法，此时佳欢0处于RUNNABLE状态、佳欢1处于BLOCKED状态(on object monitor|waiting for monitor entry)
     *      佳欢0执行sleep(30000)方法，此时佳欢0处于TIMED_WAITING状态(sleeping|waiting on condition)、佳欢1处于BLOCKED状态(on object monitor|waiting for monitor entry)
     *		佳欢0睡眠时间结束且释放锁对象之后，此时佳欢1处于RUNNABLE状态
     *      佳欢1执行wait(30000)方法，此时佳欢1处于TIMED_WAITING状态(on object monitor|in Object.wait())
     *      佳欢1睡眠时间结束且释放锁对象之后，此时佳欢1处于RUNNABLE状态
     */
    private static void blocked4() {
        ThreadState object = new ThreadState();
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                object.sleepBlocked();
            }, "佳欢"+i).start();
        }
    }
    private synchronized void sleepBlocked(){
        try {
            System.out.println(Thread.currentThread().getName()+"开始运行");
            Thread.sleep(30000);
            System.out.println(Thread.currentThread().getName()+"结束运行");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查看线程状态需要先掌握在idea中手动debug多线程，可参考：../safe/Singleton.java
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
//        runnable(); // 查看线程的NEW、Runnable、Terminated状态

//        blocked1(); // 查看线程的Blocked状态

        blocked2(); // 查看线程的Blocked状态

//        blocked3(); // 查看线程的Blocked状态

//        block/ed4(); // 查看线程的Blocked状态
    }

}
