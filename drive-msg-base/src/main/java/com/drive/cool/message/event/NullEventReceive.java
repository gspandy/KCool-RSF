/**
 * kevin 2015年8月2日
 */
package com.drive.cool.message.event;

/**
 * 空的事件接收处理器，事件调用完成后不做其他处理。
 * @author kevin
 *
 */
public class NullEventReceive implements IEventReceiver {

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#callback(com.drive.cool.message.event.IEvent)
	 */
	@Override
	public void onReceive(String orignalAlias, Object response) {

	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#isNull()
	 */
	@Override
	public boolean isNotNull() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEventReceiver#getResponse()
	 */
	@Override
	public IEvent getResponse() {
		return null;
	}

}
