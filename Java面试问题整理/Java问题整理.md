# 线程

## Java线程池七大参数详解

**JDK1.8线程池参数源代码：**

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          RejectedExecutionHandler handler) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         Executors.defaultThreadFactory(), handler);
}
```

**一、corePoolSize**

指的是核心线程大小，线程池中维护一个最小的线程数量，即使这些线程处于空闲状态，也一直存在池中，除非设置了**核心线程超时时间**。

这也是源码中的注释说明。

```java
/** @param corePoolSize the number of threads to keep in the pool, 
* even if they are idle, unless {@code allowCoreThreadTimeOut} is set.
*/
```

**二、maximumPoolSize**

指的是**线程池中允许的最大线程数量**。当线程池中核心线程都处理执行状态，有**新请求的任务**：

**1、工作队列未满：新请求的任务加入工作队列**

**2、工作队列已满：线程池会创建新线程，来执行这个任务**。当然，创建新线程不是无限制的，因为会受到maximumPoolSize最大线程数量的限制。

**三、keepAliveTime**

指的是**空闲线程存活时间**。具体说，当线程数大于核心线程数时，空闲线程在等待新任务到达的最大时间，如果超过这个时间还没有任务请求，该空闲线程就会被销毁。

可见官方注释：

```mysql
/** @param keepAliveTime when the number of threads is greater than
  *        the core, this is the maximum time that excess idle threads
  *        will wait for new tasks before terminating.
*/
```

**四、unit**

是指**空闲线程存活时间的单位**。keepAliveTime的计量单位。枚举类型TimeUnit类。

**五、workQueue**

**1、ArrayBlockingQueue**

基于数组的有界阻塞队列，特点**FIFO**（先进先出）。

**当线程池中已经存在最大数量的线程时候，再请求新的任务，这时就会将任务加入工作队列的队尾，一旦有空闲线程，就会取出队头执行任务。因为是基于数组的有界阻塞队列，所以可以避免系统资源的耗尽**。

那么如果出现有界队列已满，最大数量的所有线程都处于执行状态，这时又有新的任务请求，怎么办呢？

这时候会采用**Handler拒绝策略**，对请求的任务进行处理。后面会详细介绍。

**2、LinkedBlockingQueue**

基于链表的无界阻塞队列，默认最大容量Integer.MAX_VALUE( ![2^{32}-1](https://latex.codecogs.com/gif.latex?2%5E%7B32%7D-1))，可认为是**无限队列**，特点FIFO。

关于maximumPoolSize参数在工作队列为LinkedBlockingQueue时候，是否起作用这个问题，我们需要视情况而定！

> **情况①**：如果指定了工作队列大小，比如core=2，max=3，workQueue=2，任务数task=5，这种情况的最大线程数量的限制是有效的。
>
> **情况②**：如果工作队列大小默认![2^{32}-1](https://latex.codecogs.com/gif.latex?2%5E%7B32%7D-1)，这时maximumPoolSize不起作用，因为新请求的任务一直可以加到队列中。

**3、PriorityBlockingQueue**

优先级无界阻塞队列，前面两种工作队列特点都是FIFO，而**优先级阻塞队列可以通过参数Comparator实现对任务进行排序，不按照FIFO执行**。

**4、SynchronousQueue**

不缓存任务的阻塞队列，它实际上**不是真正的队列，因为它没有提供存储任务的空间**。生产者一个任务请求到来，会直接执行，也就是说**这种队列在消费者充足的情况下更加适合**。因为这种队列没有存储能力，所以只有当另一个线程（消费者）准备好工作，put（入队）和take（出队）方法才不会是阻塞状态。

以上四种工作队列，跟线程池结合就是一种**生产者-消费者 设计模式**。生产者把新任务加入工作队列，消费者从队列取出任务消费，BlockingQueue可以使用任意数量的生产者和消费者，这样实现了解耦，简化了设计。

**六、threadFactory**

线程工厂，创建一个新线程时使用的工厂，可以用来设定线程名、是否为daemon线程等。

> 守护线程(Daemon Thread) 在Java中有两类线程：用户线程 (User Thread)、守护线程 (Daemon Thread)。
>
> 所谓守护线程，是指在程序运行的时候在后台提供一种通用服务的线程，比如垃圾回收线程就是一个很称职的守护者，并且这种线程并不属于程序中不可或缺的部分。因此，当所有的非守护线程结束时，程序也就终止了，同时会杀死进程中的所有守护线程。反过来说，只要任何非守护线程还在运行，程序就不会终止。
>
> 用户线程和守护线程两者几乎没有区别，唯一的不同之处就在于虚拟机的离开：如果用户线程已经全部退出运行了，只剩下守护线程存在了，虚拟机也就退出了。
>
> 因为没有了被守护者，守护线程也就没有工作可做了，也就没有继续运行程序的必要了。
>
> 将线程转换为守护线程可以通过调用Thread对象的setDaemon(true)方法来实现。在使用守护线程时需要注意一下几点：
>
> (1) thread.setDaemon(true)必须在thread.start()之前设置，否则会跑出一个IllegalThreadStateException异常。你不能把正在运行的常规线程设置为守护线程。
>
> (2) 在Daemon线程中产生的新线程也是Daemon的。
>
> (3) 守护线程应该永远不去访问固有资源，如文件、数据库，因为它会在任何时候甚至在一个操作的中间发生中断。

官方使用默认的线程工厂源码如下：

```java
/**
 * The default thread factory

*/
static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
        Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
            poolNumber.getAndIncrement() +
            "-thread-";

    }

    public Thread newThread(Runnable r) {

        Thread t = new Thread(group, r,

                              namePrefix + threadNumber.getAndIncrement(),

                              0);
        if (t.isDaemon())                t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)

            t.setPriority(Thread.NORM_PRIORITY);
        return t

    }
}
```

**七、handler**

Java 并发超出线程数和工作队列时候的任务请求处理策略，使用了**策略设计模式**。

**策略1：ThreadPoolExecutor.AbortPolicy（默认）**

在默认的处理策略。**该处理在拒绝时抛出RejectedExecutionException，拒绝执行。**

```java
public static class AbortPolicy implements RejectedExecutionHandler {
    /**
     * Creates an {@code AbortPolicy}.
    */
    public AbortPolicy() { }
    /**
     * Always throws RejectedExecutionException.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     * @throws RejectedExecutionException always
    */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        throw new RejectedExecutionException("Task " + r.toString() +
                                             " rejected from " +
                                             e.toString());
    }
}
```

**策略2：ThreadPoolExecutor.CallerRunsPolicy**

**调用 execute 方法的线程本身运行任务**。这提供了一个简单的反馈控制机制，可以降低新任务提交的速度。

```java
public static class CallerRunsPolicy implements RejectedExecutionHandler {
    /**
     * Creates a {@code CallerRunsPolicy}.
    */
    public CallerRunsPolicy() { }
    /**
     * Executes task r in the caller's thread, unless the executor
     * has been shut down, in which case the task is discarded.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
    */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
        if (!e.isShutdown()) {
            r.run();
        }
	}
}
```

**策略3：ThreadPoolExecutor.DiscardOldestPolicy**

如果**执行程序未关闭，则删除工作队列头部的任务**，然后重试执行(可能再次失败，导致重复执行)。

```java
public static class DiscardOldestPolicy implements RejectedExecutionHandler {



    /**
     * Creates a {@code DiscardOldestPolicy} for the given executor.



    */
    public DiscardOldestPolicy() { }


    /**
     * Obtains and ignores the next task that the executor      
     * * would otherwise execute, if one is immediately available,
     * and then retries execution of task r, unless the executor
     * is shut down, in which case task r is instead discarded.     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
    */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {

            e.getQueue().poll();

            e.execute(r);

        }     
    }
}
```

**策略4：ThreadPoolExecutor.DiscardPolicy**

```java
public static class DiscardPolicy implements RejectedExecutionHandler {
    /**
     * Creates a {@code DiscardPolicy}.
    */

    public DiscardPolicy() { }
    /**
	 * Does nothing, which has the effect of discarding task r.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
    */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {

    }    
}
```

**八、ThreadPoolExecutor线程池参数设置技巧**

**一、ThreadPoolExecutor的重要参数**

* corePoolSize：核心线程数
  * 核心线程会一直存活，及时没有任务需要执行
  * 当线程数小于核心线程数时，即使有线程空闲，线程池也会优先创建新线程处理
  * 设置allowCoreThreadTimeout=true（默认false）时，核心线程会超时关闭
* queueCapacity：任务队列容量（阻塞队列）
  * 当核心线程数达到最大时，新任务会放在队列中排队等待执行

* maxPoolSize：最大线程数
  * 当线程数>=corePoolSize，且任务队列已满时。线程池会创建新线程来处理任务
  * 当线程数=maxPoolSize，且任务队列已满时，线程池会拒绝处理任务而抛出异常

* keepAliveTime：线程空闲时间
  * 当线程空闲时间达到keepAliveTime时，线程会退出，直到线程数量=corePoolSize
  * 如果allowCoreThreadTimeout=true，则会直到线程数量=0
* allowCoreThreadTimeout：允许核心线程超时
* rejectedExecutionHandler：任务拒绝处理器
  * 两种情况会拒绝处理任务：
    * 当线程数已经达到maxPoolSize，切队列已满，会拒绝新任务
    * 当线程池被调用shutdown()后，会等待线程池里的任务执行完毕，再shutdown。如果在调用shutdown()和线程池真正shutdown之间提交任务，会拒绝新任务
  * 线程池会调用rejectedExecutionHandler来处理这个任务。如果没有设置默认是AbortPolicy，会抛出异常
  * ThreadPoolExecutor类有几个内部实现类来处理这类情况：
    * AbortPolicy 丢弃任务，抛运行时异常
    * CallerRunsPolicy 执行任务
    * DiscardPolicy 忽视，什么都不会发生
    * DiscardOldestPolicy 从队列中踢出最先进入队列（最后一个执行）的任务
  * 实现RejectedExecutionHandler接口，可自定义处理器

**二、ThreadPoolExecutor执行顺序：**

线程池按以下行为执行任务

1. 当线程数小于核心线程数时，创建线程。

2. 当线程数大于等于核心线程数，且任务队列未满时，将任务放入任务队列。

3. 当线程数大于等于核心线程数，且任务队列已满

   1) 若线程数小于最大线程数，创建线程

   2. 若线程数等于最大线程数，抛出异常，拒绝任务

# 锁

## JVM 本地锁

一、以下三种情况可能导致 JVM 本地锁失效

1. 多例模式

```java
@Scope(Value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

2. 开启事务

```java
@Transactional(默认隔离级别是数据库的隔离级别，因为mysql隔离级别是可重复读，所以会产生问题)
```

3. 集群部署

## MySQL 锁

更新数量时进行判断

解决：1. 锁范围问题 (行级锁，表级锁) 2. 同一个商品有多条库存记录 3. 无法记录库存变化前后的状态

> 行级锁都是基于索引的。如果一条 SQL 语句用不到索引是不会使用行级锁的，而会使用表级索把整个表锁住。
>
> MySQL悲观锁中使用行级锁：1. 锁的查询或者更新条件必须是索引字段。
