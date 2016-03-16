/**
 * kevin 2015年9月27日
 */
package com.drive.cool.message.event;

/**
 * @author kevin
 *
 */
public interface IEventError {
	/**
	 * 请求超时
	 */
	public static final int TIMEOUT = -900001;
	/**
	 * 连接超时
	 */
	public static final int CONNECT_ERROR = -900002;
	/**
	 * 无可用通道
	 */
	public static final int NO_CHANNEL = -900003;
	
}
