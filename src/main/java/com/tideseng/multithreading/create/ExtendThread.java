package com.tideseng.multithreading.create;

/**
 * 继承Thred类创建线程
 * Thread类本质上是Runnable接口的实现类，是线程的对象
 *
 * JVM、HotSpot、OpenJDK
 *      JVM是Java虚拟机规范
 *      HostSpot是JVM概念的实现
 *      OpenJDK是在JDK基础上开发了HotSpot的开源项目
 *
 * 线程启动原理
 *      调用start()方法后会启动一个线程（调用start()方法的语义是当前线程告诉JVM启动调用start()方法的线程）
 *      start()是被synchronized修饰的方法，实际调用了被native修饰的start0()方法来针对不同的操作系统来创建线程并启动线程
 *      通过下方地址查看线程中的本地方法对应的JVM方法
 *          http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/00cd9dc3c2b5/src/share/native/java/lang/Thread.c
 *              {"start0",           "()V",        (void *)&JVM_StartThread},
 *              {"interrupt0",       "()V",        (void *)&JVM_Interrupt},
 *      通过hotspot/openjdk源码查找JVM_StartThread方法
 *          在hotspot\hotspot-87ee5ee27509\src\share\vm\prims\jvm.cpp文件中搜索JVM_StartThread方法
 *              JVM_ENTRY(void, JVM_StartThread(JNIEnv* env, jobject jthread))
 *                  ...
 *                  native_thread = new JavaThread(&thread_entry, sz); // 调用JavaThread创建线程
 *                  ...
 *                  Thread::start(native_thread); // 调用start方法启动线程
 *                  ...
 *              JVM_END
 *          JVM_StartThread调用了hotspot\hotspot-87ee5ee27509\src\share\vm\runtime\thread.cpp文件中的JavaThread和start
 *              JavaThread::JavaThread(ThreadFunction entry_point, size_t stack_sz)
 *                  ...
 *                  os::create_thread(this, thr_type, stack_sz); // 调用os平台创建线程
 *                  ...
 *
 *              void Thread::start(Thread* thread) {
 *                  trace("start", thread);
 *                  if (!DisableStartThread) {
 *                      if (thread->is_Java_thread()) {
 *                          // 设置线程状态为Runnable
 *                          java_lang_Thread::set_thread_status(((JavaThread*)thread)->threadObj(), java_lang_Thread::RUNNABLE);
 *                      }
 *                      os::start_thread(thread); // 调用os平台启动线程（启动后最终会调用JavaThread::run()方法来回调线程的run方法）
 *                  }
 *              }
 *      当run()方法执行完毕后，线程终止
 */
public class ExtendThread extends Thread {

    /**
     * 不指定线程名，线程名默认格式为：Thread-n（n从0开始）
     */
    public ExtendThread() {}

    /**
     * 指定线程名
     * @param name
     */
    public ExtendThread(String name) {
        super(name);
    }

    /**
     * 子线程执行逻辑
     */
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println("子线程_" + Thread.currentThread().getName() + ": " + i); // 子线程的线程名为：佳欢 / Thread-0
        }
    }

    /**
     * 主线程执行逻辑
     */
    private static void main(){
        for (int i = 0; i < 5; i++) {
            System.out.println("主线程_" + Thread.currentThread().getName() + ": "  + i); // 主线程的线程名为：main
        }
    }

    /**
     * 多线程运行原理：
     *  JVM启动并执行main()方法时，通过OS开辟一条main()方法通向cpu的路径，这个路径就是main线程/主线程，cpu通过这个线程执行main方法
     *  当调用Thread类的start()方法时，会调用被native修饰的start0()方法，OS底层开辟一条通向cpu的路径，即启动一个新线程，并执行run()方法
     *  这两个线程会不断的抢夺cpu的执行权，谁抢到就执行谁，导致主线程和子线程执行逻辑出现随机性
     *  每个线程有各自独立的栈内存空间，互不影响。所以当主线程执行完毕后，子线程仍然会执行（当子线程是守护线程时除外）
     * @param args
     */
    public static void main(String[] args) {
        ExtendThread thread1 = new ExtendThread("佳欢"); // 指定线程名称
        ExtendThread thread2 = new ExtendThread(); // 不指定线程名称，使用默认线程名称，格式为：Thread-n（n从0开始）

        thread1.start(); // 启动新线程、并执行新线程的run()方法
        thread2.start();

        main(); // 执行主线程的main()方法
    }

}
