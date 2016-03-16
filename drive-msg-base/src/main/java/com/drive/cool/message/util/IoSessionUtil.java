/**
 * kevin 2015年7月26日
 */
package com.drive.cool.message.util;

import org.apache.mina.core.session.IoSession;

import com.drive.cool.tool.util.Constants;

/**
 * @author kevin
 *
 */
public class IoSessionUtil {
	/**
	 * 获取session的别名
	 * @param session
	 * @return
	 */
	public static String getSessionAlias(IoSession session){
		return String.class.cast(session.getAttribute(Constants.SESSION_ALIAS));
	}
	
	/**
	 * 路由里，获取消息发送端的session的别名
	 * @param session
	 * @return
	 */
	public static String getOrignalAlias(IoSession session){
		return String.class.cast(session.getAttribute(Constants.ORINAL_SESSION_ALIAS));
	}
}
