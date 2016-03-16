/**
 * kevin 2015年8月7日
 */

package com.drive.cool.msg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.handler.AbstractHandler;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.msg.client.session.IoSessionPoolFactory;
import com.drive.cool.tool.util.Constants;

/**
 * 处理两种消息：<br>
 * 客户端发送经由路由转发的请求<br>
 * 客户端发送回来的响应<br>
 * @author kevin
 *
 */
public class MinaServiceHandler extends AbstractHandler{
	private static final Log log = LogFactory.getLog(MinaServiceHandler.class);
	
	@Override
	public void messageReceived(IoSession session, Object message){
		if(log.isDebugEnabled()) log.debug("接收到消息:" + session);
		if(message instanceof Event){
			Event event = Event.class.cast(message);
			if(event.isRequest()){
				//说明是service接受到的路由的请求
				if(log.isDebugEnabled()) log.debug("处理请求:" + session + event);
				IEvent response = EventFactory.getNewResponse(event.getEventId(), event.getEventType());
				doService(event, response);
				session.write(response);
				if(log.isDebugEnabled()) log.debug("处理请求完成:" + session + event);
			}else{
				//响应回来了，回调处理
				if(log.isDebugEnabled()) log.debug("处理响应:" + session + event);
				doResponse(session, event);
				if(log.isDebugEnabled()) log.debug("处理响应完成:" + session + event);
			}
		}else{
			IEvent response  = EventFactory.getNewResponse();
			setErrorToResponse(response, "不支持的消息类型");
			session.write(response);
		}
		
	}
	

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandlerAdapter#sessionOpened(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
	}
	
	// 一个客户端关闭时
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		NioSocketConnector connector = NioSocketConnector.class.cast(session.getAttribute(Constants.SESSION_CONNECTOR));
		connector.dispose(false);
		IoSessionPoolFactory.addFailurServer(session);
		super.sessionClosed(session);
	}
	
}
