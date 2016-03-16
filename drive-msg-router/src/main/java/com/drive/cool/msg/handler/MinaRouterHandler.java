/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.handler;

import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.drive.cool.message.event.Event;
import com.drive.cool.message.event.EventRegister;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.event.IEventReceiver;
import com.drive.cool.message.handler.AbstractHandler;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.message.util.EventPackUtil;
import com.drive.cool.message.util.IoSessionUtil;
import com.drive.cool.message.util.MsgConstants;
import com.drive.cool.msg.event.RouterEventReceiver;
import com.drive.cool.msg.exception.NoSuchFuncException;
import com.drive.cool.msg.exception.NotLoginException;
import com.drive.cool.msg.router.IEventRoute;
import com.drive.cool.msg.thread.IThreadPoolFactory;
/**
 * 路由消息，只负责转发请求<br>
 * @author kevin
 *
 */
public class MinaRouterHandler extends AbstractHandler{

	private IEventRoute eventRoute;
	private static final Log log = LogFactory.getLog(MinaRouterHandler.class);
	private IThreadPoolFactory threadPoolFactory;
	/**
	 *  客户端发送的消息到达时<br>
	 *  
	 */
	@Override
	public void messageReceived(final IoSession session, final Object message){
		if(message instanceof byte[]){
			getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					byte[] buffer = byte[].class.cast(message);
					String func = EventPackUtil.getFunc(buffer);
					char sendType  = EventPackUtil.getSendType(buffer);
					int orignalEventId = EventPackUtil.getEventId(buffer);
					if(IEvent.EVENT_SEND == sendType){
						char eventType = EventPackUtil.getEventType(buffer);
						if(IEvent.EVENT_REGISTER == eventType){
							//说明是service或者client的注册消息
							eventRoute.register(func, session, buffer);
						}else if(IEvent.EVENT_HEARTBEAT == eventType){
							//说明是心跳包 
							//if(log.isDebugEnabled()) log.debug("心跳包应答:" + session);
							Event response = Event.class.cast(EventFactory.getNewResponse(orignalEventId, IEvent.EVENT_HEARTBEAT));
							sendResponse(session, response);
						}else{
							//说明是client发送的消息
							//转发消息到service。
							String msg = "处理开始";
							try{
								transRequest(session, buffer, func);
							}catch(NoSuchFuncException e){
								sendErrorResponse(session, buffer, "不支持的功能：" + func);
								msg += " 处理异常";
							}catch(NotLoginException e){
								sendErrorResponse(session, buffer, "请登陆");
							}catch (Exception e) {
								//此时，可能eventId已经被重置了
								EventPackUtil.resetEventId(buffer, orignalEventId);
								sendErrorResponse(session, buffer, "请求处理异常:" + e.getMessage());
								msg += " 处理很异常";
							}
							msg += " 处理结束";
							log.debug(msg);
						}
					}else{
						//回复消息到client，此时session是服务端的session
						if(log.isDebugEnabled()) log.debug("回复消息：[" + func + "] " + session);
						doResponse(buffer);
					}
				}
			});
		}else{
			IEvent response = EventFactory.getNewResponse();
			setErrorToResponse(response, "不支持的消息类型");
			sendResponse(session,Event.class.cast(response));
		}
	}

	/**
	 * @param session
	 * @param buffer
	 * @param func
	 */
	private void sendErrorResponse(IoSession session, byte[] buffer, String errorInfo) {
		IEvent response = EventFactory.getNewResponse();
		int orignalEventId = EventPackUtil.getEventId(buffer);
		response.setEventId(orignalEventId);
		setErrorToResponse(response, errorInfo);
		sendResponse(session,Event.class.cast(response));
	}

	private void sendResponse(IoSession session, Event response){
		byte[] buffer = EventPackUtil.pack(response);
		session.write(buffer);
	}
	
	/**
	 * 获取服务端的ioSession，路由取到的是service的ioSession
	 * @param func
	 * @return
	 */
	protected IoSession getIoSession(String func){
		return eventRoute.getSession(func);
	}
	
	protected void resetUserId(IoSession session, byte[] buffer){
		
	}
	/**
	 * @param session
	 * @param buffer
	 * @param func
	 */
	private void transRequest(IoSession session, byte[] buffer, String func) {
		IoSession serviceSession = getIoSession(func);
		//调用下面语句说明支持该功能，发送到service里的是重新生成的eventId
		if(!func.equals(MsgConstants.CLIENT_USER_LOGIN)){
			resetUserId(session, buffer);
		}
		int eventId = EventPackUtil.getEventId(buffer);
		int newEventId = Event.getNextEventId();
		EventPackUtil.resetEventId(buffer, newEventId);
		if(log.isDebugEnabled()) log.debug("转发请求:[orignalEventId]=" + eventId + "[newEventId]=" + newEventId + "[func]=" + func + serviceSession);
		String sessionAlias = IoSessionUtil.getSessionAlias(session);
		String serverAlias = IoSessionUtil.getSessionAlias(serviceSession);
		//在receiver里写入的是client发过来的原始的eventId
		IEventReceiver receiver = getReceiver(session, eventId, func);
	
		EventRegister register = new EventRegister(sessionAlias, serverAlias, buffer, receiver);
		getReceiveCallback().register(register);
		if(serviceSession.isClosing()){
			getReceiveCallback().unRegister(register);
			throw new RuntimeException("服务器异常断开");
		}else{
			serviceSession.write(buffer);
		}
	}
	
	protected IEventReceiver getReceiver(IoSession session, int eventId, String func){
		return new RouterEventReceiver(session, eventId, eventRoute);
	}
	
	/**
	 * 从service接收请求 返回客户端
	 * @param sessionAlias
	 * @param response
	 */
	public void doResponse(byte[] response){
		getReceiveCallback().callback(response);
	}

	/**
	 * @return the eventRoute
	 */
	public IEventRoute getEventRoute() {
		return eventRoute;
	}

	/**
	 * @param eventRoute the eventRoute to set
	 */
	public void setEventRoute(IEventRoute eventRoute) {
		this.eventRoute = eventRoute;
	}

	// 一个客户端关闭时
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if(log.isInfoEnabled()) log.info("session关闭[sessionAlias]=" + IoSessionUtil.getSessionAlias(session));
		eventRoute.unRegister(session);
		super.sessionClosed(session);
	}

	// 一个客户端接入时
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if(log.isInfoEnabled()) log.info(session.getRemoteAddress() + "建立连接");
	}
	
	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandlerAdapter#sessionOpened(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		
	}
	
	public Executor getExecutor() {
		return this.threadPoolFactory.getExecutor();
	}

	public void setThreadPoolFactory(IThreadPoolFactory threadPoolFactory) {
		this.threadPoolFactory = threadPoolFactory;
	}

}
