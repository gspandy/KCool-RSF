/**
 * kevin 2015年9月27日
 */
package com.drive.cool.message.event;

/**
 * @author kevin
 *
 */
public class TimeoutEvent extends Event {
	
	public TimeoutEvent() {
		setErrorNo(TIMEOUT);
		setErrorInfo("请求超时");
		setEventType(EVENT_NORMAL);
		setSendType(EVENT_RECEIVE);
	}

	public TimeoutEvent(int eventId){
		this();
		setEventId(eventId);
	}
}
