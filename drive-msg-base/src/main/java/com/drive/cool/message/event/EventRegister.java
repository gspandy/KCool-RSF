/**
 * kevin 2015年8月2日
 */
package com.drive.cool.message.event;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.drive.cool.message.util.EventPackUtil;
import com.drive.cool.tool.tree.IRBTreeNode;

/**
 * 事件注册对象
 * @author kevin
 *
 */
public class EventRegister implements IRBTreeNode<EventRegister>{
	public static final IEventReceiver NULL_RECEIVER = new NullEventReceive();
	private static final long TIME_PER_SECOND = 1000L;
	private Object request;
	private IEventReceiver receiver;
	private String sessionAlias;
	private String key;
	private String serverAlias;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReadLock readLock = lock.readLock();
	private final WriteLock writeLock = lock.writeLock();
	private Condition condition = writeLock.newCondition();
	
	/**
	 * 移除时间，当前时间加上超时时间。
	 */
	private long outtime;
	
	/**
	 * 
	 */
	public EventRegister(String sessionAlias, Object request) {
		this(sessionAlias, request, NULL_RECEIVER);
	}
	
	private void setKey(){
		int eventId = 0;
		if(this.request instanceof Event){
			eventId = Event.class.cast(this.request).getEventId();
		}else if(this.request instanceof byte[]){
			eventId = EventPackUtil.getEventId(byte[].class.cast(this.request));
		}
		this.key = String.valueOf(eventId);
	}
	/**
	 * 
	 */
	public EventRegister(String sessionAlias, Object request, IEventReceiver receiver) {
		this.setSessionAlias(sessionAlias);
		this.request = request;
		this.receiver = receiver;
		int timeout = 0;
		if(request instanceof Event){
			timeout = Event.class.cast(request).getTimeout();	
		}else if(request instanceof byte[]){
			timeout = EventPackUtil.getTimeout(byte[].class.cast(request));	
		}
		this.outtime = (new Date()).getTime() + TIME_PER_SECOND * timeout;
		setKey();
	}
	
	public EventRegister(String sessionAlias, String serverAlias, Object request, IEventReceiver receiver) {
		this(sessionAlias, request, receiver);
		this.setServerAlias(serverAlias);
	}
	
	
	/**
	 * @return the receiver
	 */
	public IEventReceiver getReceiver() {
		return receiver;
	}
	/**
	 * @param receiver the receiver to set
	 */
	public void setReceiver(IEventReceiver receiver) {
		this.receiver = receiver;
	}
	/**
	 * @return the request
	 */
	public Object getRequest() {
		return request;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/**
	 * 返回-1排在前面 返回1排在后面 对于相同的，早加进来的排在前面，所以返回1
	 */
	@Override
	public int compareTo(EventRegister o) {
		return getOuttime() < o.getOuttime() ? -1 : 1;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.tool.tree.IRBTreeNode#getKey()
	 */
	@Override
	public String getKey() {
		return this.key;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.tool.tree.IRBTreeNode#getReadLock()
	 */
	@Override
	public ReadLock getReadLock() {
		return readLock;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.tool.tree.IRBTreeNode#getWriteLock()
	 */
	@Override
	public WriteLock getWriteLock() {
		return writeLock;
	}

	/**
	 * @return the outtime
	 */
	public long getOuttime() {
		return outtime;
	}

	/**
	 * @param outtime the outtime to set
	 */
	public void setOuttime(long outtime) {
		this.outtime = outtime;
	}
	
	
	public void notifyLock() {
		writeLock.lock();
		try {
			condition.signal();
		} finally {
			writeLock.unlock();
		}
	}
	
	public boolean lock(long callBackTime) {
		boolean result = false;
		writeLock.lock();
		try {
			result = condition.await(callBackTime, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return result;
	}

	/**
	 * @return the sessionAlias
	 */
	public String getSessionAlias() {
		return sessionAlias;
	}

	/**
	 * @param sessionAlias the sessionAlias to set
	 */
	public void setSessionAlias(String sessionAlias) {
		this.sessionAlias = sessionAlias;
	}

	public String getServerAlias() {
		return serverAlias;
	}

	public void setServerAlias(String serverAlias) {
		this.serverAlias = serverAlias;
	}
	
}
