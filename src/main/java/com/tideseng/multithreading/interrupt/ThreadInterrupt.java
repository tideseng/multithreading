package com.tideseng.multithreading.interrupt;

import java.util.concurrent.TimeUnit;

/**
 * 线程终止
 *      stop()方法已过期，stop()方法结束了线程时并不会保证线程资源正常释放，因此会导致程序出现问题
 *      调用interrupt()方法后会终止一个线程
 *          其它线程调用当前线程的interrupt()方法，告诉当前线程执行中断操作
 *          当前线程检查自身是否被中断(isInterrupted)进行相应处理（什么时候中断，取决于线程处理逻辑）
 *      interrupt()会调用同步代码块中被native修饰的interrupt0()方法来终止一个线程，底层是将isInterrupted设置成true（实际是修改Thread.isInterrupted state，还会唤醒线程）
 *      通过hotspot/openjdk源码查找JVM_Interrupt方法
 *          在hotspot\hotspot-87ee5ee27509\src\share\vm\prims\jvm.cpp文件中搜索JVM_Interrupt方法
 *              JVM_ENTRY(void, JVM_Interrupt(JNIEnv* env, jobject jthread))
 *                  ...
 *                  Thread::interrupt(thr);
 *                  ...
 *              JVM_END
 *          JVM_Interrupt调用了hotspot\hotspot-87ee5ee27509\src\share\vm\runtime\thread.cpp文件中的interrupt
 *              void Thread::interrupt(Thread* thread) {
 *                  trace("interrupt", thread);
 *                  debug_only(check_for_dangling_thread_pointer(thread);)
 *                  os::interrupt(thread); // // 调用os平台终止线程
 *              }
 *          linux的os路径为hotspot\hotspot-87ee5ee27509\src\os\linux\vm\os_linux.cpp
 *              void os::interrupt(Thread* thread) {
 *                  ...
 *                  OSThread* osthread = thread->osthread(); // 获取OSThread
 *                  if (!osthread->interrupted()) {
 *                      osthread->set_interrupted(true); // 调用OSThread的set_interrupted方法将_interrupted设置为true
 *                      OrderAccess::fence();
 *                      ParkEvent * const slp = thread->_SleepEvent ;
 *                      if (slp != NULL) slp->unpark() ; // 通过ParkEvent的unpark方法来唤醒线程
 *                  }
 *                  ...
 *              }
 *          os::interrupt调用了hotspot\hotspot-87ee5ee27509\src\share\vm\runtime\osThread.hpp文件的set_interrupted
 *              volatile jint _interrupted;     // Thread.isInterrupted state
 *              void set_interrupted(bool z) { _interrupted = z ? 1 : 0; }
 */
public class ThreadInterrupt {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(()->{
            while(!Thread.currentThread().isInterrupted()){} // 检查是否被终止，底层判断的是_interrupted的状态
            System.out.println("线程终止");
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        thread.interrupt(); // 终止线程，将isInterrupted设置成true
    }

}
