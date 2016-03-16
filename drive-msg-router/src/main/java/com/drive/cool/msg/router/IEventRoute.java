/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.router;

import org.apache.mina.core.session.IoSession;

/**
 * 服务向路由注册
 * @author kevin
 *
 */
public interface IEventRoute {
	
	
	/**
	 * client断开
	 * @param session
	 */
	public void unRegister(IoSession session);
	
	/**
	 * 服务端注册
	 * @param func
	 * @param session
	 * @param buffer 发送的Event数据<br>
	 * 如果是service注册，event里包含注册的服务<br>
	 * 如果是client注册，event包含session的别名<br>
	 */
	public void register(String func, IoSession session, byte[] buffer);
	
	/**
	 * 根据调用的功能返回session<br>
	 * 取到的session是服务端session<br>
	 * @param func
	 * @return
	 */
	public IoSession getSession(String func);

	
	/**
	 * 根据session的别名取session<br>
	 * 取得的 session是客户端的session<br>
	 * @param sessionAlias
	 * @return
	 */
	public IoSession getOriginalSession(String sessionAlias);
}
