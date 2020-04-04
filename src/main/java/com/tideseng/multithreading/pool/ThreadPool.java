package com.tideseng.multithreading.pool;

import java.util.concurrent.*;

/**
 * 线程池，集合容器
 * Executors: 线程池的工厂类，用于创建线程池
 */
public class ThreadPool {

    static class RunnableImpl implements Runnable {
        @Override
        public void run() {
            System.out.println("RunnableImpl: " + Thread.currentThread().getName());
        }
    }

    static class CallableImpl implements Callable<String>{
        @Override
        public String call() throws Exception {
            System.out.println("CallableImpl：" + Thread.currentThread().getName());
            return "佳欢";
        }
    }

    /**
     * 线程池会一直开启，所以程序运行完成并不会退出，通过调用shutdown()方法销毁线程退出程序
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 生产指定线程数量的线程池
        Future<?> future1 = executorService.submit(new RunnableImpl()); // 执行Runnable的任务
        Future<String> future2 = executorService.submit(new CallableImpl()); // 执行Callable的任务
        System.out.println(future1.get()); // 获取Runnable的任务返回值，Runnable没有返回值，所以一律为null
        System.out.println(future2.get()); // 获取Callable的任务返回值
    }

}
