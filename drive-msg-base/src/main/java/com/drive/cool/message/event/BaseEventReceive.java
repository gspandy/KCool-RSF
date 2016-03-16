/**
 * kevin 2015年8月2日
 */
package com.drive.cool.message.event;


/**
 * 通用事件接收处理器
 * @author kevin
 *
 */
public class BaseEventReceive implements IEventReceiver {

	private IEvent response;
	
	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceive#callback(com.drive.cool.message.event.IEvent)
	 */
	@Override
	public void onReceive(String orignalAlias, Object response) {
		//这里不能有异常，异常会被 handler的exceptionCaught捕捉掉
		this.response = Event.class.cast(response);
	}
	
	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#isNull()
	 */
	@Override
	public boolean isNotNull() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#getResponse()
	 */
	@Override
	public IEvent getResponse() {
		return response;
	}

}
