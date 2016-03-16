/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.handler;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.MethodInvoker;

import com.drive.cool.message.callback.IReceiveCallback;
import com.drive.cool.message.conf.ServiceConfig;
import com.drive.cool.message.dataset.IDataset;
import com.drive.cool.message.dataset.SimpleObjectDataset;
import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.service.IServiceFactory;
import com.drive.cool.message.util.DatasetUtil;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.message.util.MsgConstants;
import com.drive.cool.tool.util.Constants;

/**
 * 事件处理基础类
 * @author kevin
 *
 */
public class AbstractHandler extends RegisterHandler implements ApplicationContextAware {
	protected static final Log log = LogFactory.getLog(AbstractHandler.class);
	
	private IServiceFactory serviceFactory;
	private IReceiveCallback receiveCallback;
	private static ApplicationContext ctx;
	
	
	/**
	 * @param request
	 * @param response
	 * @param config
	 */
	protected void doService(Event request, IEvent response, ServiceConfig config) {
		IDataset result;
		try{
			if(request.isNormal()){
				IDataset dataset = request.getDataset();
				result = exeSpringMethod(config, dataset);
				response.setDataset(result);
			}else{
				response.setErrorInfo("尚未支持");
			}
		}catch(Exception e){
			response.setErrorInfo(e.getMessage());
		}
	}
	
	/**
	 * @param message
	 * @param response
	 */
	protected void doService(Event request, IEvent response) {
		
		String func = request.getFunc();
		ServiceConfig config = serviceFactory.getServiceConfig(func);
		if(null == config){
			String errorInfo =  "不支持的功能:" + func;
			setErrorToResponse(response, errorInfo);
		}else{
			doService(request, response, config);
		}
	}
	
	/**
	 * 调用spring的方法
	 * @param springConfig
	 * @param param
	 */
	private IDataset exeSpringMethod(ServiceConfig springConfig, IDataset dataset){
		Object bean = springConfig.getBean();
		String method = springConfig.getMethod();
		
		
		MethodInvoker methodInvoker = new MethodInvoker();
		methodInvoker.setTargetObject(bean);
		methodInvoker.setTargetMethod(method);
		
		Object param = dataset.getValue();
		if(IDataset.MULTI_OBJECT == dataset.getType() && param instanceof Object[]){
			methodInvoker.setArguments(Object[].class.cast(param));
		}else{
			methodInvoker.setArguments(new Object[]{param});
		}
		Object rv = null;
		try {
			methodInvoker.prepare();
			rv = methodInvoker.invoke();
		} catch (Exception e) {
			if(e instanceof InvocationTargetException){
				throw new RuntimeException(InvocationTargetException.class.cast(e).getTargetException());
			}else{
				throw new RuntimeException(e);
			}
		} 
		return DatasetUtil.parse(rv);
	}
	
	/**
	 * @param response
	 * @param errorInfo
	 */
	protected void setErrorToResponse(IEvent response, String errorInfo) {
		log.error(errorInfo);
		response.setErrorInfo(errorInfo);
	}

	/**
	 * client接收到发出去的请求的响应<br>
	 * @param session
	 * @param message
	 */
	protected void doResponse(IoSession session, Object message) {
		receiveCallback.callback(message);
	}
	

	// 一个客户端关闭时
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.info(session.getRemoteAddress() + "断开连接");
	}

	// 一个客户端接入时
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		log.info(session.getRemoteAddress() + "建立连接");
	}
	

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		log.error("数据发送异常" ,  cause);
		session.close(true);
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

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}
	
	/**
	 * 如果当前是service提供服务，注册路由信息<br>
	 * @param session
	 */
	public void register(IoSession session){
		String sessionAlias = String.valueOf(session.getAttribute(Constants.SESSION_ALIAS));
		if(null != serviceFactory){
			serviceFactory.register(session, sessionAlias);
		}else{
			registerClient(session, sessionAlias);
		}
	}
	
	protected void sendEvent(IoSession session, IEvent event){
		session.write(event);
	}
	
	private void registerClient(IoSession session, String sessionAlias){
		IEvent clientLoginEvent = EventFactory.getNewRequest(IEvent.EVENT_REGISTER);
		clientLoginEvent.setFunc(MsgConstants.CLIENT_SESSION_LOGIN);
		IDataset dataset = new SimpleObjectDataset<String>(sessionAlias);
		clientLoginEvent.setDataset(dataset);
		sendEvent(session, clientLoginEvent);
	}
}
