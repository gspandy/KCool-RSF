/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.callback;

import com.drive.cool.message.event.EventRegister;

/**
 * 事件处理回调接口
 * @author kevin
 *
 */
public interface IReceiveCallback {
	
	/**
	 * 注册回调事件
	 * @param sessionAlias 客户端所在别名
	 * @Param register 注册的event
	 */
	public void register(EventRegister register);
	
	
	/**
	 * 回调处理
	 * @param response 返回的event
	 * @return
	 */
	public void callback(Object response);
	
	/**
	 * 取消注册，在调用超时时移除register
	 * @param register
	 */
	public void unRegister(EventRegister register);
	
	
	/**
	 * 取消注册，在服务端异常关闭时移除register
	 * @param sessionAlias
	 * @param isService 是不是服务端，如果是服务端，说明在路由上断开了服务端；<br>
	 * 	如果不是服务端，说明是客户端断开了路由或者路由断开了客户端<br>
	 */
	public void unRegister(String sessionAlias, boolean isService);
}
