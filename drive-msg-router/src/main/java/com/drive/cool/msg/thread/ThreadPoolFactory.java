/**
 * kevin 2015年9月27日
 */
package com.drive.cool.msg.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kevin
 *
 */
public class ThreadPoolFactory implements IThreadPoolFactory {

	/**
	 * 默认最大线程数
	 */
	private static final int DEFAULT_MAX_POOL_SIZE = 16;
	/**
	 * 默认线程数
	 */
	private static final int DEFAULT_THREAD_NUMBER = 0;

	/**
	 * 默认最大队列数
	 */
	private static final int DEFUALTMAXQUEUESIZE = 100;

	/**
	 * 默认线程持续时间
	 */
	private static final int DEFAULT_KEEPALIVETIME = 30;

	/**
	 * 默认时间计数单位
	 */
	private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;

	private int maxQueueSize;

	private ThreadPoolExecutor executor = null;
	
	/**
	 * 
	 */
	public ThreadPoolFactory() {
		super();
		executor = new ThreadPoolExecutor(DEFAULT_THREAD_NUMBER, DEFAULT_MAX_POOL_SIZE, DEFAULT_KEEPALIVETIME,
				DEFAULT_TIMEUNIT, new ArrayBlockingQueue<Runnable>(DEFUALTMAXQUEUESIZE),
				new ThreadPoolExecutor.AbortPolicy());
	}
	
	public ThreadPoolFactory(int corePoolSize, int maximumPoolSize, int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, DEFAULT_KEEPALIVETIME, DEFAULT_TIMEUNIT,
				new ArrayBlockingQueue<Runnable>(this.maxQueueSize), new ThreadPoolExecutor.AbortPolicy());
	}
	
	/* (non-Javadoc)
	 * @see com.drive.cool.msg.thread.IThreadPoolFactory#getExecutor()
	 */
	@Override
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.msg.thread.IThreadPoolFactory#getMaxTaskQueueNumber()
	 */
	@Override
	public int getMaxTaskQueueNumber() {
		return maxQueueSize;
	}

}
