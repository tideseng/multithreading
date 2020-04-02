package com.tideseng.multithreading.create;

import java.util.concurrent.*;

/**
 * 实现Callable接口并通过FutureTask包装器创建线程
 */
public class ImplCallable implements Callable<String> {

    /**
     * 子线程执行逻辑
     * @return
     * @throws Exception
     */
    @Override
    public String call() throws Exception {
        for (int i = 0; i < 5; i++) {
            System.out.println("子线程：" + Thread.currentThread().getName()); // 佳欢 / Thread-0
        }
        return Thread.currentThread().getName() + "返回结果";
    }

    /**
     * 主线程执行逻辑
     */
    private static void main(){
        for (int i = 0; i < 5; i++) {
            System.out.println("主线程：" + Thread.currentThread().getName()); // main
        }
    }

    /**
     * 两个子线程并发处理，主线程等到子线程执行完毕后才开始执行
     * 子线程处理逻辑时主线程处于WAIT状态，当子线程执行完毕后主线程处于RUNNING状态（子线程处于WAIT状态）
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 创建固定数量的线程池
        Future<String> future1 = executorService.submit(new ImplCallable()); // pool-1-thread-1
        Future<String> future2 = executorService.submit(new ImplCallable()); // pool-1-thread-2
        System.out.println(future1.get()); // 获取子线程返回结果
        System.out.println(future2.get()); // 获取子线程返回结果
        executorService.shutdown(); // 关闭线程，否则子线程一直处于WAIT状态，导致程序不会退出

        main();
    }

}
