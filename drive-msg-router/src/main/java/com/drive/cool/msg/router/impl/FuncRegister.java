/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.router.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.drive.cool.message.util.IoSessionUtil;
import com.drive.cool.msg.exception.NoSuchFuncException;

/**
 * 服务功能注册
 * @author kevin
 *
 */
public class FuncRegister {
	
	/**
	 * funcIoSession的key为sessionAlias<br>
	 * value为该别名对应的所有注册过的IoSession<br>
	 */
	private Map<String, List<IoSession>> funcIoSession;
	private List<String> sessionAliasList;
	private Lock lock = new ReentrantLock();
	private int currAliasIndex;
	private int currSessionIndex;
	private String func;
	private static final Log log = LogFactory.getLog(FuncRegister.class);
	/**
	 * 
	 */
	public FuncRegister() {
		funcIoSession = new HashMap<String, List<IoSession>>();
		sessionAliasList = new ArrayList<String>();
	}
	
	public FuncRegister(String func) {
		this();
		this.func = func;
	}
	
	/**
	 * 从当前功能号注册的session里取一个返回<br>
	 * 目前采用均衡获取的方式<br>
	 * @return
	 */
	public IoSession getSession(){
		lock.lock();
		try{
			//1. 从注册的里面顺序取一个sesssionAlias
			int length = sessionAliasList.size();
			if(0 == length){
				throw new NoSuchFuncException("无可用连接");
			}
			currAliasIndex = (currAliasIndex + 1)%length;
			if(0 == currAliasIndex){ //这里简单处理，每轮流切换一次alias，增加一个index。
				currSessionIndex++;
			}
			String sessionAlias = sessionAliasList.get(currAliasIndex);
			List<IoSession> sessionList = funcIoSession.get(sessionAlias);
			int sessionLength = sessionList.size();
			int currIndex = currSessionIndex%sessionLength;
			IoSession session = sessionList.get(currIndex);
			if(log.isDebugEnabled()){
				String msg = "获取[func=" + func +"]功能对应的session,共" + length + "个服务端提供该功能，"
				 + "当前取第" + currAliasIndex + "个服务端，"
				 + "该服务端共" + sessionLength + "个session,"
				 + "当前取第" + currIndex + "个session";
				log.debug(msg);
			}
			return session;
		}finally{
			lock.unlock();
		}
	}
	
	public void addIoSession(IoSession session){
		lock.lock();
		try{
			String sessionAlias = IoSessionUtil.getSessionAlias(session);
			List<IoSession> sessionList = funcIoSession.get(sessionAlias);
			if(null == sessionList){
				sessionList = new ArrayList<IoSession>();
				funcIoSession.put(sessionAlias, sessionList);
				sessionAliasList.add(sessionAlias);
				if(log.isInfoEnabled()){
					log.info("[func="+func+"]添加sessionAlias注册[sessionAlias]=" + sessionAlias);
				}
			}
			sessionList.add(session);
			if(log.isInfoEnabled()){
				log.info("[func="+func+"]添加session注册[sessionAlias]=" + sessionAlias + " " + session);
			}
		}finally{
			lock.unlock();
		}
	}
	
	
	public void removeIoSession(IoSession session){
		lock.lock();
		try{
			String sessionAlias = IoSessionUtil.getSessionAlias(session);
			List<IoSession> sessionList = funcIoSession.get(sessionAlias);
			if(null == sessionList){
				return;
			}
			
			for(IoSession ioSession : sessionList){
				if(ioSession.getId() == session.getId()){
					sessionList.remove(ioSession);
					if(log.isInfoEnabled()) log.info("[func=" + func +"]移除session注册[sessionAlias]=" + sessionAlias + " " + session);
					break;
				}
			}
			if(0 == sessionList.size()){
				funcIoSession.remove(sessionAlias);
				sessionAliasList.remove(sessionAlias);
				if(log.isInfoEnabled()) log.info("[func=" + func +"]移除sessionAlias注册[sessionAlias]=" + sessionAlias);
			}
		}finally{
			lock.unlock();
		}
	}
}
