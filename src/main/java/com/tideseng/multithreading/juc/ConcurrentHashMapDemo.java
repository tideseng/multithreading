package com.tideseng.multithreading.juc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程安全的HashMap
 */
public class ConcurrentHashMapDemo {

    public static void main(String[] args) {
        ConcurrentHashMap map = new ConcurrentHashMap(); // 线程安全
        map.put(1, "佳欢");

        Map<Integer, String> hashMap = new HashMap<Integer, String>();
        Collections.synchronizedMap(hashMap); // 将不安全的集合设置成安全的
    }

}
