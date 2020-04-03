package com.tideseng.multithreading.safe;

/**
 * 多线程安全问题
 *  i++并不是原子性操作，而是有三个步骤：getstatic读取值、iadd增加值、putstatic设置值（通过javap -c ThreadSafe2.class可以查看）
 *  当多个线程先后执行读取到的值相同时，就会导致设置的值相同，产生线程安全问题
 */
public class ThreadSafe2 {

    private static int count;

    public synchronized static void add() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        count++;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++)
            new Thread(() -> ThreadSafe2.add()).start();
        Thread.sleep(1000);
        System.out.println("运行结果: " + count);
    }

}
