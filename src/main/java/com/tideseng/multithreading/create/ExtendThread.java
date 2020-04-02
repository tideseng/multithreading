package com.tideseng.multithreading.create;

/**
 * 继承Thred类创建线程
 * Thread类本质上是Runnable接口的实现类，是线程的对象
 */
public class ExtendThread extends Thread {

    /**
     * 不指定线程名，线程名默认格式为：Thread-n（n从0开始）
     */
    public ExtendThread() {}

    /**
     * 指定线程名
     * @param name
     */
    public ExtendThread(String name) {
        super(name);
    }

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
     * @param args
     */
    public static void main(String[] args) {
        ExtendThread thread1 = new ExtendThread("佳欢"); // 指定线程名称
        ExtendThread thread2 = new ExtendThread(); // 不指定线程名称，使用默认线程名称，格式为：Thread-n（n从0开始）

        thread1.start(); // 启动新线程、并执行新线程的run()方法
        thread2.start();

        main(); // 执行主线程的main()方法
    }

}
