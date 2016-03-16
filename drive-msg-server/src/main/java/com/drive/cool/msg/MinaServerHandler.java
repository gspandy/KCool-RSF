/**
 * kevin 2015年8月8日
 */
package com.drive.cool.msg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.handler.AbstractHandler;
import com.drive.cool.message.util.EventFactory;

/**
 * 只负责处理客户端发过来的请求，和路由没有关系<br>
 * @author kevin
 *
 */
public class MinaServerHandler extends AbstractHandler{
	
	private static final Log log = LogFactory.getLog(MinaServerHandler.class);
	// 客户端发送的消息到达时
	@Override
	public void messageReceived(IoSession session, Object message){
		IEvent response;
		if(message instanceof Event){
			Event request = Event.class.cast(message);
			if(log.isDebugEnabled()) log.debug("处理请求：" + session + request);
			response = EventFactory.getNewResponse(request.getEventId(), request.getEventType());
			doService(request, response);
			if(log.isDebugEnabled()) log.debug("处理请求完成：" + session + request);
		}else{
			response = EventFactory.getNewResponse();
			setErrorToResponse(response, "不支持的消息类型");
		}
		session.write(response);
	}

	
}
