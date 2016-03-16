/**
 * kevin 2015年8月3日
 */
package com.drive.cool.message.util;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;

/**
 * 事件工厂，根据需求初始化事件
 * @author kevin
 *
 */
public class EventFactory {
	/**
	 * 获取新的请求event
	 * @param eventType
	 * @param func
	 * @return
	 */
	public static IEvent getNewRequest(char eventType, String func){
		Event event = new Event(eventType, func);
		event.setSendType(IEvent.EVENT_SEND);
		return event;
	}
	
	public static IEvent getNewRequest(char eventType){
		return getNewRequest(eventType, null);
	}
	
	/**
	 * 获取新的响应event
	 * @param eventType
	 * @return
	 */
	public static IEvent getNewResponse(int eventId, char eventType){
		Event event = new Event(eventId, eventType);
		event.setSendType(IEvent.EVENT_RECEIVE);
		return event;
	}
	
	public static IEvent getNewResponse(){
		Event event = new Event(IEvent.EVENT_RECEIVE);
		return event;
	}
}
