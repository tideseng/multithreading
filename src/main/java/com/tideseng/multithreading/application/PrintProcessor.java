package com.tideseng.multithreading.application;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 责任链之打印请求
 */
public class PrintProcessor extends Thread implements IRequestProcessor {

    // 阻塞队列
    private LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<Request>();
    // 下一个处理器
    private IRequestProcessor nextProcessor;
    // 是否关闭的标志
    private volatile boolean isFinished;

    public PrintProcessor() {
    }

    public PrintProcessor(IRequestProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    /**
     * 异步处理请求
     */
    @Override
    public void run() {
        while (!isFinished){
            try {
                Request request = requests.take();// 阻塞式获取数据
                System.out.println("PrintProcessor: " + request); // 处理逻辑
                if(nextProcessor != null) nextProcessor.process(request); // 交给下一个责任链
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(Request request) {
        this.requests.add(request); // 将请求加入队列异步进行处理
    }

    /**
     * 对外提供关闭的方法
     */
    @Override
    public void shutdown(){
        isFinished = true;
        if(nextProcessor != null) nextProcessor.shutdown();
    }
}
