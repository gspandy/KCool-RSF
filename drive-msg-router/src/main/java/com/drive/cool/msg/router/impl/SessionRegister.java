/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.router.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.session.IoSession;

/**
 * mina会话注册
 * @author kevin
 *
 */
public class SessionRegister {
	public static final char SESSION_CLIENT = 0x01;
	public static final char SESSION_SERVICE = 0x02;
	
	private char registerType;
	private List<IoSession> ioSessionList;
	private Lock lock = new ReentrantLock();
	private int currIndex = 0;
	/**
	 * 
	 */
	public SessionRegister() {
		this.ioSessionList = new ArrayList<IoSession>();
	}
	
	public void register(IoSession session){
		lock.lock();
		try{
			this.ioSessionList.add(session);
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * 获取一个可用的session
	 * @return
	 */
	public IoSession getSession(){
		lock.lock();
		try{
			int length = ioSessionList.size();
			if(0 == length){
				return null;
			}
			currIndex = (currIndex+1)%length;
			return ioSessionList.get(currIndex);
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * session断开时，取消注册<br>
	 * @param session
	 */
	public void unRegister(IoSession session){
		lock.lock();
		try{
			for(int i = 0; i < ioSessionList.size(); i++){
				IoSession ioSession = ioSessionList.get(i);
				if(session.getId() == ioSession.getId()){
					//移除session
					ioSessionList.remove(i);
					break;
				}
			}
		}finally{
			lock.unlock();
		}
	}

	
	public boolean isService() {
		return SESSION_SERVICE == registerType;
	}
	
	public boolean isClient() {
		return SESSION_CLIENT == registerType;
	}

	
	public void setToClient() {
		this.registerType = SESSION_CLIENT;
	}
	
	public void setToService() {
		this.registerType = SESSION_SERVICE;
	}
}
