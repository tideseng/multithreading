package com.tideseng.multithreading.application;

public interface IRequestProcessor {

    void process(Request request);

    void shutdown();

}
