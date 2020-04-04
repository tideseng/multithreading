package com.tideseng.multithreading.application;

public class App {

    private static IRequestProcessor requestProcessor;

    static {
        // 构建责任链
        SaveProcessor saveProcessor = new SaveProcessor();
        PrintProcessor printProcessor = new PrintProcessor(saveProcessor);
        PreProcessor preProcessor = new PreProcessor(printProcessor);
        saveProcessor.start();
        printProcessor.start();
        preProcessor.start();

        requestProcessor = preProcessor;
    }

    public static void main(String[] args) throws InterruptedException {
        Request request = new Request("佳欢");

        requestProcessor.shutdown(); // 关闭

        requestProcessor.process(request); // 执行

    }
}
