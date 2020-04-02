package com.tideseng.multithreading.create;

/**
 * 匿名内部类创建线程
 */
public class AnonymousClass {

    private void run(){
        for (int i = 0; i < 5; i++) {
            System.out.println("子线程_" + Thread.currentThread().getName() + ": " + i);
        }
    }

    private void main(){
        for (int i = 0; i < 5; i++) {
            System.out.println("主线程_" + Thread.currentThread().getName() + ": "  + i); // 主线程的线程名为：main
        }
    }

    public static void main(String[] args) {
        AnonymousClass object = new AnonymousClass();

        // 重写Thread类的run()方法
        new Thread("佳欢"){
            @Override
            public void run() {
                object.run();
            }
        }.start();

        // 实现Runnable的run()方法
        new Thread(new Runnable() {
            @Override
            public void run() {
                object.run();
            }
        }, "金昌").start();

        // 实现Runnable的run()方法
        new Thread(() -> {
            object.run();
        }, "何鑫").start();

        object.main();
    }

}
