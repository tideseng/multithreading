package com.tideseng.multithreading.reset;

import java.util.concurrent.TimeUnit;

/**
 * 线程复位（线程回到初始状态）
 *      1.调用Thread.interrupted()方法
 *      2.产生InterruptedException异常
 * 线程复位是当前线程对外界发出终止线程的响应，具体是否中断及什么时候中断由当前线程决定
 */
public class ThreadReset {

    private static void interrupted() throws InterruptedException {
        Thread thread=new Thread(()->{
            while (true) {
                while (Thread.currentThread().isInterrupted()) { // 检查是否被中断，底层判断的是_interrupted的状态
                    System.out.println("线程终止");
                    Thread.interrupted(); // 线程复位，isInterrupted又回到初始状态
                    System.out.println("线程复位");
                    return;
                }
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        thread.interrupt(); // 将isInterrupted设置成true
    }

    private static void interruptedException() throws InterruptedException {
        Thread thread=new Thread(()->{
            while(!Thread.currentThread().isInterrupted()){ // 检查是否被中断，底层判断的是_interrupted的状态
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) { // 终止处于阻塞状态的线程，会唤醒线程、线程复位（清除线程中断标识）、抛出InterruptedException异常
                    System.out.println("线程复位");
                    e.printStackTrace();
                    return;
                }
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        thread.interrupt(); // 将isInterrupted设置成true
    }

    public static void main(String[] args) throws InterruptedException {
//        interrupted();

        interruptedException();
    }

}
