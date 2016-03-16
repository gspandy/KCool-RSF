/**
 * kevin 2015年8月2日
 */
package com.drive.cool.message.event;

/**
 * 事件接收处理接口
 * @author kevin
 *
 */
public interface IEventReceiver {
	
	/**
	 * 获取应答event
	 * @return
	 */
	public IEvent getResponse();
	
	/**
	 * 接收到消息时的回调
	 * @param orignalAlias client的session的别名
	 * @param response
	 */
	public void onReceive(String orignalAlias, Object response);
	
	/**
	 * 是否空的receiver
	 */
	public boolean isNotNull();
}
