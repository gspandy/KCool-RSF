/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.callback.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.drive.cool.message.callback.IReceiveCallback;
import com.drive.cool.message.event.BaseEventReceive;
import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.EventRegister;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.event.IEventReceiver;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.message.util.EventPackUtil;
import com.drive.cool.tool.tree.RBTree;

/**
 * 通用事件处理回调器
 * @author kevin
 *
 */
public class ReceiveCallback implements IReceiveCallback{

	private final RBTree<EventRegister> RECEIVER_TREE = new RBTree<EventRegister>();
	private final Map<String, HashSet> REGISTER_MAP = new HashMap<String, HashSet>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	private static final Log log = LogFactory.getLog(ReceiveCallback.class);
	
	/* (non-Javadoc)
	 * @see com.drive.cool.msg.client.callback.IReceiveCallback#register(com.drive.cool.message.event.EventRegister)
	 */
	@Override
	public void register(EventRegister register) {
		w.lock();
		try{
			if(null == register.getReceiver()){
				register.setReceiver(new BaseEventReceive());
			}
			if(register.getReceiver().isNotNull()){
				RECEIVER_TREE.insertNode(register);	
				HashSet set = getSessionRegisterSet(register.getServerAlias());
				set.add(register.getKey());
			}
		}finally{
			w.unlock();
		}
	}
	
	private HashSet getSessionRegisterSet(String sessionAlias){
		HashSet set = REGISTER_MAP.get(sessionAlias);
		if(null == set){
			set = new HashSet();
			REGISTER_MAP.put(sessionAlias, set);
			if(log.isInfoEnabled()) log.debug("添加sessionAlias事件注册[sessionAlias=" + sessionAlias + "]");
		}
		return set;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.msg.client.callback.IReceiveCallback#callback(com.drive.cool.message.event.IEvent)
	 */
	@Override
	public void callback(Object response) {
		String eventId = null;
		if(response instanceof Event){
		  eventId = String.valueOf(Event.class.cast(response).getEventId());
		}else if(response instanceof byte[]){
			eventId = String.valueOf(EventPackUtil.getEventId(byte[].class.cast(response)));
		}
		EventRegister register = null; 
		w.lock();
		try{
			register = RECEIVER_TREE.getDataByKey(eventId);
			if(null != register){
				register.getReceiver().onReceive(register.getSessionAlias(),response);
			}
		}finally{
			if(null != register){
				register.notifyLock();
				RECEIVER_TREE.deleteNode(register);
				HashSet set = getSessionRegisterSet(register.getServerAlias());
				set.remove(register.getKey());
			}
			w.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.callback.IReceiveCallback#unRegister(com.drive.cool.message.event.EventRegister)
	 */
	@Override
	public void unRegister(EventRegister register) {
		w.lock();
		try{
			RECEIVER_TREE.deleteNode(register);
			HashSet set = getSessionRegisterSet(register.getServerAlias());
			set.remove(register.getKey());
		}finally{
			w.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.callback.IReceiveCallback#unRegister(java.lang.String)
	 */
	@Override
	public void unRegister(String sessionAlias, boolean isService) {
		w.lock();
		try{
			Set<String> registerSet =  REGISTER_MAP.get(sessionAlias);
			if(null == registerSet) {
				log.info("当前没有已经注册的事件需要移除");
				return;
			}
			Iterator<String> it = registerSet.iterator();
			if(log.isInfoEnabled()){
				String msg = "移除断开的session的所有已经注册的事件[sessionAlias=" 
						+ sessionAlias + "]，一共[" + registerSet.size() + "]个事件";
				log.info(msg);
			}
			while (it.hasNext()) {  
				//key其实是eventId
			    String key = it.next();  
			    EventRegister register = RECEIVER_TREE.getDataByKey(key);
			    if(null == register) continue;
			    try{
				    IEventReceiver receiver = register.getReceiver();
				    if(null != receiver && receiver.isNotNull()){
				    	IEvent response = EventFactory.getNewResponse(Integer.valueOf(key), IEvent.EVENT_NORMAL);
				    	response.setErrorInfo("服务异常断开");
				    	if(receiver instanceof BaseEventReceive){
					    	receiver.onReceive(sessionAlias, response);
				    	}else{ //目前先认为其他的就是 RouterEventReceiver，除了这个以外都继承BaseEventReceive
				    		receiver.onReceive(sessionAlias, EventPackUtil.pack(Event.class.cast(response)));
				    	}
				    }
			    }finally{
			    	if(null != register){
						register.notifyLock();
						RECEIVER_TREE.deleteNode(register);
					}
			    	it.remove();
			    }
			    
			}  
			REGISTER_MAP.remove(sessionAlias);
			if(log.isInfoEnabled()) log.debug("移除sessionAlias事件注册[sessionAlias=" + sessionAlias + "]");
		}finally{
			w.unlock();
		}
	}

}
