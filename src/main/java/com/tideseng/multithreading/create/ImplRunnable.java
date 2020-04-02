package com.tideseng.multithreading.create;

/**
 * 实现Runnable接口创建线程
 * Runnable对象是Thread对象的target、Runnable实现类的run()方法仅作为线程的执行体。
 *  实际的线程对象依然是Thread实例，只是Thread实例负责调用其target的run()方法
 * 实现Runnable接口的优势：
 *      避免单继承的局限性
 *      增强了程序的扩展性，降低了程序的耦合性
 *      便于线程池管理（线程池只能放实现了Runnable或Callable的对象）
 */
public class ImplRunnable implements Runnable {

    /**
     * 子线程执行逻辑
     */
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println("子线程_" + Thread.currentThread().getName() + ": " + i); // 子线程的线程名为：佳欢 / Thread-0
        }
    }

    /**
     * 主线程执行逻辑
     */
    private static void main(){
        for (int i = 0; i < 5; i++) {
            System.out.println("主线程_" + Thread.currentThread().getName() + ": "  + i); // 主线程的线程名为：main
        }
    }

    /**
     * 多线程运行原理：
     *  JVM启动并执行main()方法时，通过OS开辟一条main()方法通向cpu的路径，这个路径就是main线程/主线程，cpu通过这个线程执行main方法
     *  当调用Thread类的start()方法时，会调用被native修饰的start0()方法，OS底层开辟一条通向cpu的路径，即启动一个新线程，并执行run()方法
     *  这两个线程会不断的抢夺cpu的执行权，谁抢到就执行谁，导致主线程和子线程执行逻辑出现随机性
     *  每个线程有各自独立的栈内存空间，互不影响。所以当主线程执行完毕后，子线程仍然会执行（当子线程是守护线程时除外）
     *  当线程执行完毕后，线程自动在栈内存中释放；当所有线程都结束时，进程结束
     * @param args
     */
    public static void main(String[] args) {
        Thread thread1 = new Thread(new ImplRunnable(), "佳欢"); // 通过构造函数指定线程名
        Thread thread2 = new Thread(new ImplRunnable());
        thread2.setName("金昌"); // 通过方法执行线程名

        thread1.start(); // 启动新线程、并执行新线程的run()方法
        thread2.start();

        main(); // 执行主线程的main()方法
    }

}
