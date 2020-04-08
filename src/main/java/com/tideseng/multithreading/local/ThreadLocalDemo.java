package com.tideseng.multithreading.local;

/**
 * 本地线程
 *      提供了一个线程范围的局部变量（线程级别的隔离）
 * ThreadLocal结构
 *      static class ThreadLocalMap{ // 一个线程可以存储多个不同的本地线程实例
 *          private static final int INITIAL_CAPACITY = 16;
 *          private Entry[] table;
 *          static class Entry extends WeakReference<ThreadLocal<?>> {
 *              Object value;
 *              Entry(ThreadLocal<?> k, Object v) { // 当前ThreadLocal对象为key、返回值为value
 *                  super(k);
 *                  value = v;
 *              }
 *          }
 *      }
 * 线程的变量副本存储方式
 * ThreadLocal初始化设置
 */
public class ThreadLocalDemo {

    private static int count = 0;
    private static ThreadLocal<Integer> countLocal;
    private static ThreadLocal<String> strLocal = ThreadLocal.withInitial(() -> "佳欢");

    static {
        countLocal = new ThreadLocal<Integer>(){
            /**
             * ThreadLocal初始化设置值
             *
             * 调用时机：调用get()方法获取当前线程的ThreadLocalMap为空时
             *      初始化ThreadLocalMap，并初始化Entry数组，创建Entry元素（当前ThreadLocal对象为key、返回值为value）并放入数组中
             *      斐波那契散列/黄金分割散列实现均衡的散列
             * @return
             */
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
    }

    private static final int HASH_INCREMENT = 0x61c88647;
    private static void maginHash(int size){
        int hashcode;
        for (int i = 0; i < size; i++) {
            hashcode = i * HASH_INCREMENT + HASH_INCREMENT;
            System.out.print((hashcode & (size -1)) + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            new Thread(() -> {
                count++;
                countLocal.set(countLocal.get()+1);
                System.out.println(Thread.currentThread().getName() + ": " + count + ", " + countLocal.get());
            }).start();
        }
        maginHash(16); // 只能是2的n次方
    }

}
