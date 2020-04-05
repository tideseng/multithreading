# 多线程

了解多线程的发展、基本使用和原理分析

## 相关目录

- [线程创建](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/create)
- [线程安全](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/safe)
- [线程同步](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/sync)
- [线程状态](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/state)
- [线程池](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/pool)
- [多线程应用](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/application)
- [线程终止](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/interrupt)
- [线程复位](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/reset)
- [线程可见性](https://github.com/tideseng/multithreading/tree/master/src/main/java/com/tideseng/multithreading/jmm)

## 基本介绍

- 并发和并行

并发：同一时间段有多个任务交替执行

并行：同一时间点有多个任务同时执行（只在多CPU中存在）

- 进程和线程

进程：进入内存运行的应用程序（每个进程都有独立的内存空间，一个应用程序可以同时运行多个进程）

线程：进程的执行单元（负责当前进程中程序的执行，一个进程中至少有一个线程）

- 线程调度

分时调度：所有线程轮流使用CPU的使用权，平均分配每个线程占用CPU的时间

抢占式调度：优先让优先级高的线程使用CPU，如果线程优先级相同则随机选择（JVM使用抢占式调度）

- 主线程

主线程：JVM中执行main()方法的线程

- 多线程意义

1. 提高CPU资源的利用率
2. 在多核CPU中可并行执行
3. 提升程序性能