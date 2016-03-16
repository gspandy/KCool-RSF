/**
 * kevin 2015年8月6日
 */
package com.drive.cool.msg.client;

import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.event.IEventReceiver;

/**
 * 客户端接口
 * @author kevin
 *
 */
public interface IMinaClient {
	/**
	 * 发送消息，返回服务器响应
	 * @param event 参数event
	 * @return 服务器响应event
	 */
	public IEvent postMessage(IEvent event);
	
	/**
	 * 发消息，不返回响应，如果需要对返回结果处理，通过回调进行处理
	 * @param event
	 * @param eventReceiver 消息接收回调，如果无需回调，传入 EventRegister.NULL_RECEIVER<br>
	 */
	public void postMessage(IEvent event, IEventReceiver eventReceiver);
	
}
