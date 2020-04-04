package com.tideseng.multithreading.application;

/**
 * 多线程应用场景：通过责任链模式处理请求（也可通过分布式消息中间件实现）
 */
public class Request {

    private String name;

    public Request(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Request{" +
                "name='" + name + '\'' +
                '}';
    }
}
