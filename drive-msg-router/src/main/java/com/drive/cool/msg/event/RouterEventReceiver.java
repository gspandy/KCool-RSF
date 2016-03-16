/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.event;

import org.apache.mina.core.session.IoSession;

import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.event.IEventReceiver;
import com.drive.cool.message.util.EventPackUtil;
import com.drive.cool.msg.router.IEventRoute;

/**
 * 路由事件接收处理器
 * @author kevin
 *
 */
public class RouterEventReceiver implements IEventReceiver {

	/**
	 * 客户端发过来的eventId
	 */
	protected int orignalEventId;
	
	protected IEventRoute eventRoute;
	
	protected IoSession session;
	
	/**
	 * 
	 */
	public RouterEventReceiver(IoSession session, int eventId) {
		this.session = session;
		this.orignalEventId = eventId;
	}
	
	public RouterEventReceiver(IoSession session, int eventId, IEventRoute eventRoute) {
		this(session, eventId);
		this.eventRoute = eventRoute;
	}
	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#getResponse()
	 */
	@Override
	public IEvent getResponse() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#onReceive(com.drive.cool.message.event.IEvent)
	 */
	@Override
	public void onReceive(String orignalAlias, Object response) {
		//接收到应答后，返回消息到发送端
		if(null == session || !session.isConnected()){
			session = eventRoute.getOriginalSession(orignalAlias);
		}
		if(null != session){
			//恢复之前的eventId
			if(response instanceof byte[]){
				EventPackUtil.resetEventId(byte[].class.cast(response), orignalEventId);
			}
			session.write(response);
		}
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#isNotNull()
	 */
	@Override
	public boolean isNotNull() {
		return true;
	}
	/**
	 * @return the eventRoute
	 */
	public IEventRoute getEventRoute() {
		return eventRoute;
	}
	/**
	 * @param eventRoute the eventRoute to set
	 */
	public void setEventRoute(IEventRoute eventRoute) {
		this.eventRoute = eventRoute;
	}

}
