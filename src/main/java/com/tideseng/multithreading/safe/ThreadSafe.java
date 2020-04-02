package com.tideseng.multithreading.safe;

/**
 * 多线程安全问题
 * 原因：
 *      多个线程修改同一个资源（全局变量或静态冰凉）
 */
public class ThreadSafe {

    private static ThreadSafe instance;

    public static ThreadSafe getInstance() {
        if(instance == null) // 当两个线程都轮流执行了该行且对象还未创建时就会生成多个实例，导致多线程安全问题
            instance = new ThreadSafe();
        return instance;
    }

    /**
     * idea中手动debug多线程模拟多线程安全问题
     *      1.在相应出打上断点（在这里是在第13行打上断点）
     *      2.单击右键、勾选Thread、点击Done
     *      3.开启Debug模式，相应线程进入断点
     *      4.在IDEA的Debugger窗口的下拉框中选择指定Thread进行调试
     *      5.让一个线程执行到第9行，切换到另外一个线程并执行到第9行，然后放行，手动让程序生成多个实例
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                ThreadSafe instance = ThreadSafe.getInstance(); // 两个线程并发运行，开始不断抢夺CPU执行权去创建实例
                System.out.println(instance);
            }).start();
        }
    }

}
