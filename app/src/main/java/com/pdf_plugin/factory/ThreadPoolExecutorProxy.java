package com.pdf_plugin.factory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorProxy {
	
	ThreadPoolExecutor mExecutor;
	
	private int mCorePoolSize;
	private int mMaximumPoolSize;
	private long mKeepAliveTime;
	public ThreadPoolExecutorProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
		super();
		this.mMaximumPoolSize=maximumPoolSize;
		this.mCorePoolSize=corePoolSize;
		this.mKeepAliveTime=keepAliveTime;
	}
	private void initThreadPool()
	{
		if(mExecutor==null||mExecutor.isShutdown()||mExecutor.isTerminating())
		{
			TimeUnit unit=TimeUnit.MILLISECONDS; //单位时间
			 LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(); 
			 ThreadFactory threadFactory = Executors.defaultThreadFactory();
			 RejectedExecutionHandler handler = new DiscardPolicy();
			synchronized (ThreadPoolExecutorProxy.class) {
				if(mExecutor==null||mExecutor.isShutdown()||mExecutor.isTerminating())
				{
//					mCorePoolSize, // 核心线程数
//					mMaximumPoolSize,// 最大线程数
//					mKeepAliveTime, // 保持时间
					mExecutor=new ThreadPoolExecutor(mCorePoolSize,//
							mMaximumPoolSize,
							mKeepAliveTime,
							unit,//事件单位
							workQueue,//工作队列
							threadFactory,//线程工厂
							handler);//异常捕获器
				}
			}
		}
	}
	/**提交任务*/
	public Future<?> submit(Runnable task) {
		initThreadPool();
		return mExecutor.submit(task);
	}

	/**执行任务*/
	public void execute(Runnable task) {
		initThreadPool();
		mExecutor.execute(task);
	}

	/**移除任务*/
	public void remove(Runnable task) {
		initThreadPool();
		mExecutor.remove(task);
	}
}
