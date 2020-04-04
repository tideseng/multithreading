package com.tideseng.multithreading.lock;

/**
 * 线程可见性
 *
 * volatile作用
 *      使得在多处理器环境下保证共享数据的可见性
 * 使用IDEA查看运行Java代码生成的汇编指令
 *      1.将hsdis-amd64.dll、hsdis-amd64.lib文件复制到jre/bin/server目录下
 *      2.编辑Configurations
 *          VM optinos处输入:-server -Xcomp -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly -XX:CompileCommand=compileonly,*ThreadVolatile.main
 *          JRE处选择jre所在目录
 * volatile关键字保证可见性
 *      在修改带有volatile修饰的成员变量时，会多一个lock指令
 *      lock是一种控制指令，在多处理器环境下，lock汇编指令可以基于总线锁或者缓存锁的机制来达到可见性效果（lock指令相当于内存屏障的一种实现）
 * 可见性
 *      为了实现跨线程写入的内存可见性
 *      硬件层面
 *          一台计算机中最核心的组件是 CPU、内存、以及 I/O 设备，但三者在处理速度的差异较大
 *          为了平衡三者的速度差异，最大化的利用 CPU 提升性能，从硬件、操作系统、编译器等方面都做出了很多的优化
 *              1.CPU增加高速缓存
 *                  尽可能接近处理器运算速度的高速缓存来作为内存和处理器之间的缓冲：将运算需要使用的数据复制到缓存中让运算能快速进行，当运算结束后再从缓存同步到内存之中
 *                  会导致缓存一致性问题（在不同 CPU 中运行的不同线程看到同一份内存的缓存值不一样）
 *                  CPU层面解决方案
 *                      总线锁
 *                          开销比较大
 *                      缓存锁
 *                          基于缓存一致性协议实现
*                   MESI协议
 *                      在MESI协议中，每个缓存的缓存控制器不仅知道自己的读写操作，而且也监听(snoop)其它缓存的读写操作
 *                      缓存行的四种状态
 *                          M(Modify)
 *                              数据只缓存在当前CPU缓存中，并且是被修改状态（缓存的数据和主内存中的数据不一致）
 *                          E(Exclusive)
 *                              数据只缓存在当前CPU缓存中，并且没有被修改
 *                          S(Shared)
 *                              数据可能被多个CPU缓存，并且各个缓存中的数据和主内存数据一致（写数据需要先等到其他CPU中缓存行置为I无效，然后才改为M修改(阻塞)）
 *                              为了避免阻塞带来的资源浪费，在cpu中引入Store Bufferes
 *                                  写数据时，直接把数据写入到storebufferes中， 同时发送invalidate消息，然后继续去处理其他指令
 *                                  当收到ack消息时，再将store bufferes中的数据数据存储至缓存行中，最后再从缓存行同步到主内存
 *                              Store Bufferes带来的问题
 *                                  数据修改后什么时候提交是不确定，导致其它失效的CPU从主内存读的数据仍然不是修改后的（CPU的重排序-->带来可见性问题）
 *                              CPU层面提供Memory Barrier内存屏障指令解决高速缓存的重排序导致的可见性问题
 *                                  Store Memory Barrier(写屏障)
 *                                      使得写屏障之前的指令的结果对屏障之后的读或者写是可见的（让处理器在写屏障之前在Store Bufferes中的数据同步到主内存）
 *                                  Load Memory Barrier(读屏障)
 *                                      配置写屏障，使得写屏障之前的内存更新对读屏障之后的读操作是可见的（让处理器在读屏障之后的读操作从主内存中去读数据）
 *                                  Full Memory Barrier(全屏障)
 *                                      使得屏障前的内存读写操作的结果提交到内存之后，再执行屏障后的读写操作
 *                          I(Invalid)
 *                              缓存已失效（和其它CPU缓存的数据不一致，读数据从主内存读取）
 *              2.引入进程、线程（通过 CPU 的时间片切换最大化的提升 CPU 的使用率）
 *              3.编译器的指令优化（重排序）
 *      JMM(Java Memory Model)层面
 *          提供了合理的禁用缓存以及禁止重排序的方法解决可见性和有序性
 *          JMM属于语言级别的抽象内存模型
 *              定义了共享内存中多线程程序读写操作的行为规范
 *              在虚拟机中把共享变量存储到内存以及从内存中取出共享变量的底层实现细节
 *          JMM把底层的问题抽象到JVM层面，再基于CPU层面提供的内存屏障指令，以及限制编译器的重排序来解决并发问题
 *              JMM抽象模型分为
 *                  主内存
 *                      主内存是所有线程共享
 *                          一般是实例对象、静态字段、数组对象等存储在堆内存中的变量
 *                  工作内存（类似CPU的高速缓存）
 *                      工作内存是每个线程独占的
 *                          线程对变量的所有操作都必须在工作内存中进行，线程之间的共享变量值的传递都是基于主内存来完成
 *              实现原理
 *                  通过内存屏障(memory barrier)禁止重排序
 *                  使编译器根据具体的底层体系架构，将这些内存屏障替换成具体的 CPU 指令
 *                  对于编译器而言，内存屏障将限制它所能做的重排序优化
 *                  对于处理器而言，内存屏障将会进行缓存的刷新操作
 *                  如对于 volatile，编译器将在 volatile 字段的读写操作前后各插入一些内存屏障
 *              重排序
 *                  编译器的重排序和 CPU 的重排序会遵守数据依赖性原则/as-if-serial
 *                  从源代码到最终执行的指令，可能会经过三种重排序
 *                      编译器重排序
 *                          JMM提供了禁止特定类型的编译器重排序
 *                      处理器重排序（指令集并行重排序、内存系统重排序）
 *                          JMM会要求编译器生成指令时，会插入内存屏障来禁止处理器重排序
 *              JMM层面的内存屏障
 *                  Java编译器通过关键字(volatile、 synchronized、 final)在生成指令序列的适当位置会插入内存屏障来禁止特定类型的处理器的重排序
 *                      volatile修饰的变量都会加上StoreLoad内存屏障(全屏障)，最终会调用valatile编译器级别内存屏障禁止编译器对代码优化、lockCPU级别内存屏障汇编指令锁住缓存行（不保证原子性）
 *              JMM四种内存屏障
 *                  LoadLoad
 *                  StoreStore
 *                  LoadStore
 *                  StoreLoad(全屏障)
 */
public class ThreadVolatile {

    /**
     * 当stop不被volatile修饰时，读线程不能及时的读取到其他线程写入的最新值
     * 当stop不被volatile修饰时，读线程能够及时的读取到其他线程写入的最新值
     */
    private volatile static boolean stop;

    /**
     * 主线程修改了stop变量，当子线程读取stop变量的值未true时，结束循环退出子程序
     * @throws InterruptedException
     */
    private static void exit() throws InterruptedException {
        new Thread(() -> {
            int i = 0;
            while (!stop) {
                i++;
            }
            System.out.println("子程序退出");
        }).start();
        Thread.sleep(1000);
        stop = true;
    }

    public static void main(String[] args) throws InterruptedException {
        exit();
    }

}
