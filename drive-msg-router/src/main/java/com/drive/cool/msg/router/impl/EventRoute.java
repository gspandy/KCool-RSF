/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.router.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.drive.cool.message.callback.IReceiveCallback;
import com.drive.cool.message.dataset.MapValueDataset;
import com.drive.cool.message.dataset.SimpleObjectDataset;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.util.EventPackUtil;
import com.drive.cool.message.util.IoSessionUtil;
import com.drive.cool.message.util.MsgConstants;
import com.drive.cool.msg.router.IEventRoute;
import com.drive.cool.tool.util.Constants;

/**
 * 
 * @author kevin
 *
 */
public class EventRoute implements IEventRoute{

	private static final Log log = LogFactory.getLog(EventRoute.class);
	private IReceiveCallback receiveCallback;
	/**
	 * key为func<br>
	 * value为提供该func的service的session
	 */
	private static Map<String, FuncRegister> FUNC_MAP = new HashMap<String, FuncRegister>();
	
	private static Map<String, SessionRegister> SESSION_MAP = new HashMap<String, SessionRegister>();
	private Lock lock = new ReentrantLock();
	
	private SessionRegister getSessionRegister(String sessionAlias){
		SessionRegister register = null;
		lock.lock();
		try{
			register = SESSION_MAP.get(sessionAlias);
			if(null == register){
				register = new SessionRegister();
				SESSION_MAP.put(sessionAlias, register);
			}
			return register;
		}finally{
			lock.unlock();
		}
	}
	private FuncRegister getFuncRegister(String func){
		FuncRegister register = null;
		lock.lock();
		try{
			register = FUNC_MAP.get(func);
			if(null == register){
				register = new FuncRegister(func);
				FUNC_MAP.put(func, register);
			}
			return register;
		}finally{
			lock.unlock();
		}
	}
	/* (non-Javadoc)
	 * @see com.drive.cool.msg.router.IEventRoute#register(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void register(String func, IoSession session, byte[] buffer) {
		lock.lock();
		try{
			IEvent registerEvent = EventPackUtil.unPack(buffer);
			if(MsgConstants.CLIENT_SESSION_LOGIN.equals(func)){
				registerClient(session, registerEvent);
			}else{
				registerService(session, registerEvent);
			}
		}finally{
			lock.unlock();
		}
	}
	/**
	 * @param session
	 * @param registerEvent
	 */
	private void registerService(IoSession session, IEvent registerEvent) {
		MapValueDataset dataset = MapValueDataset.class.cast(registerEvent.getDataset());
		Map<String, Object> data = dataset.getValue();
		String sessionAlias = MapUtils.getString(data, Constants.SESSION_ALIAS);
		session.setAttribute(Constants.SESSION_ALIAS, sessionAlias);
		data.remove(Constants.SESSION_ALIAS);
		
		//session注册
		if(log.isDebugEnabled()) log.debug("service的session注册:[session]=" +sessionAlias + " " + session);
		SessionRegister sessionRegister = getSessionRegister(session);
		sessionRegister.setToService();
		sessionRegister.register(session);
		
		//功能注册
		for(String registerFunc : data.keySet()){
			FuncRegister funcRegister = getFuncRegister(registerFunc);
			funcRegister.addIoSession(session);
		}
	}
	
	/**
	 * 根据调用的功能返回session<br>
	 * 取到的session是服务端session<br>
	 * @param func
	 * @return
	 */
	public IoSession getSession(String func){
		lock.lock();
		try{
			FuncRegister register = getFuncRegister(func);
			return register.getSession();
		}finally{
			lock.unlock();
		}
	}

	
	/**
	 * 根据session的别名取session<br>
	 * 取得的 session是客户端的session<br>
	 * @param sessionAlias
	 * @return
	 */
	public IoSession getOriginalSession(String sessionAlias){
		SessionRegister register = getSessionRegister(sessionAlias);
		return register.getSession();
	}
	/* (non-Javadoc)
	 * @see com.drive.cool.msg.router.IEventRoute#register(org.apache.mina.core.session.IoSession)
	 */
	private void registerClient(IoSession session, IEvent event) {
		SimpleObjectDataset<String> dataset = SimpleObjectDataset.class.cast(event.getDataset());
		String sessionAlias = dataset.getValue();
		if(log.isDebugEnabled()) log.debug("client的session注册:[session]=" +sessionAlias + " " + session);
		session.setAttribute(Constants.SESSION_ALIAS, sessionAlias);
		SessionRegister register = getSessionRegister(session);
		register.setToClient();
		register.register(session);
	}
	/* (non-Javadoc)
	 * @see com.drive.cool.msg.router.IEventRoute#unRegister(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void unRegister(IoSession session) {
		lock.lock();
		try{
			//移除功能注册
			//移除session注册
			String sessionAlias = IoSessionUtil.getSessionAlias(session);
			
			
			SessionRegister sessionRegister = getSessionRegister(session);
			//当前正在调用的全部置为异常返回
			receiveCallback.unRegister(sessionAlias, sessionRegister.isService());
			sessionRegister.unRegister(session);
			if(log.isInfoEnabled()){
				log.info("注册的session移除。[" + sessionAlias + "]" + session);
			}
			if(sessionRegister.isService()){
				for(String func : FUNC_MAP.keySet()){
					FuncRegister funcRegister = FUNC_MAP.get(func);
					funcRegister.removeIoSession(session);
				}
			}
		}finally{
			lock.unlock();
		}
	}
	/**
	 * @param session
	 * @return
	 */
	private SessionRegister getSessionRegister(IoSession session) {
		String sessionAlias = IoSessionUtil.getSessionAlias(session);
		SessionRegister register = getSessionRegister(sessionAlias);
		return register;
	}
	
	
	public IReceiveCallback getReceiveCallback() {
		return receiveCallback;
	}
	public void setReceiveCallback(IReceiveCallback receiveCallback) {
		this.receiveCallback = receiveCallback;
	}
	
	
}
