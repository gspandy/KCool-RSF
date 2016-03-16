/**
 * kevin 2015年8月6日
 */
package com.drive.cool.msg.client.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.springframework.beans.factory.annotation.Value;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.handler.AbstractHandler;
import com.drive.cool.message.util.EventPackUtil;
import com.drive.cool.message.util.IoSessionUtil;
import com.drive.cool.msg.client.session.IoSessionPoolFactory;
import com.drive.cool.tool.util.Constants;

/**
 * Mina客户端，只处理客户端发出去的消息回来的响应<br>
 * @author kevin
 *
 */
public class MinaClientHandler extends AbstractHandler {
	
	@Value("${mina.access.flag}")
	private String isAccess;
	
	private static final Log log = LogFactory.getLog(MinaClientHandler.class);
	/**
	 * 这里接收到的消息只包含
	 * 1. client发出的请求回来的应答<br>
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if(message instanceof byte[] || message instanceof IEvent ){
			if(log.isDebugEnabled()) log.debug("处理应答:" + session);
			doResponse(session, message);
		}else{
			log.error("不支持的消息类型");
			return;
		}
		
	}

	// 一个客户端关闭时
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		NioSocketConnector connector = NioSocketConnector.class.cast(session.getAttribute(Constants.SESSION_CONNECTOR));
		connector.dispose(false);
		IoSessionPoolFactory.addFailurServer(session);
		getReceiveCallback().unRegister(IoSessionUtil.getSessionAlias(session), false);
		super.sessionClosed(session);
	}
	
	protected void sendEvent(IoSession session, IEvent event){
		if("true".equals(isAccess)){
			session.write(EventPackUtil.pack(Event.class.cast(event)));
		}else{
			session.write(event);
		}
	}
	
	
	
}