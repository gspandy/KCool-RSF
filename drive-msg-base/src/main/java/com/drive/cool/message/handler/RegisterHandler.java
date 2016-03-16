/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.handler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * 事件处理基础类
 * @author kevin
 *
 */
public abstract class RegisterHandler extends IoHandlerAdapter{
	
	/**
	 * 如果当前是service提供服务，注册路由信息<br>
	 * @param session
	 */
	public abstract void register(IoSession session);
	
}
