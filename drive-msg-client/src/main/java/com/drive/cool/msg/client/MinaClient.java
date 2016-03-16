/**
 * kevin 2015年8月6日
 */
package com.drive.cool.msg.client;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.drive.cool.init.FrameInitedListener;
import com.drive.cool.message.callback.IReceiveCallback;
import com.drive.cool.message.event.BaseEventReceive;
import com.drive.cool.message.event.EventRegister;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.event.IEventReceiver;
import com.drive.cool.message.event.TimeoutEvent;
import com.drive.cool.message.service.IServiceFactory;
import com.drive.cool.message.util.IoSessionUtil;
import com.drive.cool.msg.client.conf.MinaServerConfig;
import com.drive.cool.msg.client.exception.NoIdleSessionException;
import com.drive.cool.msg.client.session.CommonIoSessionPoolFactory;
import com.drive.cool.msg.client.session.IoSessionPoolFactory;

/**
 * 客户端实现
 * @author kevin
 *
 */
public class MinaClient implements IMinaClient, ApplicationContextAware, InitializingBean, FrameInitedListener{
	private static final Log log = LogFactory.getLog(MinaClient.class);
	public static ApplicationContext ctx = null;
	
	private List<MinaServerConfig> configList;
	
	private AtomicBoolean isStarted = new AtomicBoolean(false);
	
	private IReceiveCallback receiveCallback;
	
	private IServiceFactory serviceFactory;
	
	private static IMinaClient minaClient;
	
	@Value("${mina.access.flag}")
	private String isAccess;
	
	public synchronized static IMinaClient getMinaClient(){
		if(null == minaClient){
			minaClient = ctx.getBean("minaClient", IMinaClient.class);
		}
		return minaClient;
	}
	
	private void startClient(){
		if(!isStarted.compareAndSet(false, true)){
			return;
		}
		if(null == configList){
			throw new RuntimeException("未配置服务器连接");
		}
		
		IoHandlerAdapter handler = null;
		try{
			handler = ctx.getBean("minaServiceHandler", IoHandlerAdapter.class);
		}catch(NoSuchBeanDefinitionException e){
			handler = ctx.getBean("minaClientHandler", IoHandlerAdapter.class);
		}
		
		boolean isAccess = "true".equals(this.isAccess);
		new CommonIoSessionPoolFactory(configList, handler, isAccess);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx; 
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.msg.client.IMinaClient#sendMessage(org.apache.mina.core.session.IoSession, com.drive.cool.message.event.IEvent)
	 */
	@Override
	public IEvent postMessage(IEvent event){
		IoSession session = null;
		EventRegister register = null;
		IEventReceiver eventReceiver =  new BaseEventReceive();
		try{
			session = IoSessionPoolFactory.getIoSession();
			if(null == session){
				log.error("无可用连接");
				throw new NoIdleSessionException("无可用连接");
			}
			String sessionAlias = IoSessionUtil.getSessionAlias(session);
			if(log.isDebugEnabled()) log.debug("发送请求,[sessionAlias]=" + sessionAlias + session + event);
			register = new EventRegister(sessionAlias, sessionAlias, event, eventReceiver);
			receiveCallback.register(register);
			session.write(event);
			if(log.isDebugEnabled()) log.debug("发送请求完成,[sessionAlias]=" + sessionAlias + session + event);
		}finally{
			if(null != session){
				IoSessionPoolFactory.returnIoSession(session);
			}
		}
		if(null != register) {
			if(log.isDebugEnabled()) log.debug("等待服务响应：" + event);
			if(null != eventReceiver) {
				boolean callResult = register.lock(event.getTimeout());
				if(!callResult){
					//响应超时时直接返回超时
					receiveCallback.unRegister(register);
					return getTimeoutResponse(event);
				}
			}
			if(log.isDebugEnabled()) log.debug("服务响应完成：" + event);
		}
		return eventReceiver.getResponse();
	}
	
	public IEvent getTimeoutResponse(IEvent request){
		return new TimeoutEvent(request.getEventId());
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.msg.client.IMinaClient#sendMessage(com.drive.cool.message.event.IEvent, com.drive.cool.message.event.IEventReceiver)
	 */
	@Override
	public void postMessage(IEvent event, IEventReceiver eventReceiver){
		IoSession session = null;
		EventRegister register = null;
		try{
			session = IoSessionPoolFactory.getIoSession();
			if(null == session){
				log.error("无可用连接");
				throw new NoIdleSessionException("无可用连接");
			}
			String sessionAlias = IoSessionUtil.getSessionAlias(session);
			register = new EventRegister(sessionAlias, event, eventReceiver);
			if(null != eventReceiver || eventReceiver.isNotNull()){
				receiveCallback.register(new EventRegister(sessionAlias,sessionAlias, event, eventReceiver));
			}
			session.write(event);
		}finally{
			if(null != session){
				IoSessionPoolFactory.returnIoSession(session);
			}
		}
		if(null != eventReceiver) {
			boolean callResult = register.lock(event.getTimeout());
			if(!callResult){
				receiveCallback.unRegister(register);
				//响应超时时直接触发回调
				IEvent response = getTimeoutResponse(event);
				register.getReceiver().onReceive(register.getSessionAlias(),response);
			}
		}
	}
	
	
	/**
	 * @return the configList
	 */
	public List<MinaServerConfig> getConfigList() {
		return configList;
	}

	/**
	 * @param configList the configList to set
	 */
	public void setConfigList(List<MinaServerConfig> configList) {
		this.configList = configList;
	}

	/**
	 * @return the receiveCallback
	 */
	public IReceiveCallback getReceiveCallback() {
		return receiveCallback;
	}

	/**
	 * @param receiveCallback the receiveCallback to set
	 */
	public void setReceiveCallback(IReceiveCallback receiveCallback) {
		this.receiveCallback = receiveCallback;
	}

	
	/**
	 * @return the serviceFactory
	 */
	public IServiceFactory getServiceFactory() {
		return serviceFactory;
	}

	/**
	 * @param serviceFactory the serviceFactory to set
	 */
	public void setServiceFactory(IServiceFactory serviceFactory) {
		this.serviceFactory = serviceFactory;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		try{
			this.serviceFactory = this.ctx.getBean("serviceFactory", IServiceFactory.class); 
		}catch(NoSuchBeanDefinitionException e){
			this.serviceFactory = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.init.FrameInitedListener#init()
	 */
	@Override
	public void init() {
		startClient();
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.init.FrameInitedListener#getOrder()
	 */
	@Override
	public int getOrder() {
		return 0;
	}
}
