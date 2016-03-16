/**
 * kevin 2015年9月27日
 */
package com.drive.cool.msg.thread;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kevin
 *
 */
public interface IThreadPoolFactory {
	/**
	 * 获取线程执行者
	 * 
	 * @return
	 */
	public ThreadPoolExecutor getExecutor();

	/**
	 * 获取最大列队数
	 * 
	 * @return
	 */
	public int getMaxTaskQueueNumber();
}
