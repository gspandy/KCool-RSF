/**
 * kevin 2015年8月1日
 */
package com.drive.cool.msg.client.session;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.message.util.EventPackUtil;

/**
 * @author kevin
 *
 */
public class KeepAliveFactory implements KeepAliveMessageFactory {

	/**
	 * 
	 */
	public KeepAliveFactory(boolean isAccess) {
		this.isAccess = isAccess;
	}
	
	//是否接入端drive-msg-access使用
	protected boolean isAccess = false;
		
	/* (non-Javadoc)
	 * @see org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isRequest(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public boolean isRequest(IoSession session, Object message) {
		if(isAccess){
			byte[] eventByte = byte[].class.cast(message);
			if(IEvent.EVENT_HEARTBEAT == EventPackUtil.getEventType(eventByte)
					&& IEvent.EVENT_SEND == EventPackUtil.getSendType(eventByte)){
				return true;
			}
		}else{
			Event event = Event.class.cast(message);
			if(event.isRequest() && event.isHeartbeat()){
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isResponse(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public boolean isResponse(IoSession session, Object message) {
		if(isAccess){
			byte[] eventByte = byte[].class.cast(message);
			if(IEvent.EVENT_HEARTBEAT == EventPackUtil.getEventType(eventByte)
					&& IEvent.EVENT_RECEIVE == EventPackUtil.getSendType(eventByte)){
				return true;
			}
		}else{
			Event event = Event.class.cast(message);
			if(event.isResponse() && event.isHeartbeat()){
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getRequest(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public Object getRequest(IoSession session) {
		IEvent event = EventFactory.getNewRequest(IEvent.EVENT_HEARTBEAT);
		if(isAccess){
			return EventPackUtil.pack(Event.class.cast(event));
		}else{
			return event;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getResponse(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public Object getResponse(IoSession session, Object request) {
		IEvent event = EventFactory.getNewResponse();
		event.setEventType(IEvent.EVENT_HEARTBEAT);
		if(isAccess){
			return EventPackUtil.pack(Event.class.cast(event));
		}else{
			return event;
		}
	}
}
