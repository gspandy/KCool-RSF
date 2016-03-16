/**
 * kevin 2015年9月6日
 */
package com.drive.cool.msg.client.session;

import java.util.List;
import java.util.UUID;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.drive.cool.msg.client.conf.MinaServerConfig;
import com.drive.cool.msg.client.exception.SessionCreateException;
import com.drive.cool.tool.util.Constants;

/**
 * @author kevin
 *
 */
public abstract class IoSessionPoolFactory {
	private static ObjectPool<IoSession> IO_SESSION_POOL;
	private static IoSessionFactory sessionFactory;
	//是否接入端drive-msg-access使用
	protected boolean isAccess = false;
		
	public IoSessionPoolFactory(List<MinaServerConfig> configList, IoHandlerAdapter handler, boolean isAccess) {
		this.isAccess = isAccess;
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		int totalSize = 0;
		for(MinaServerConfig serverConfig : configList){
			totalSize += serverConfig.getSize();
		}
		config.setMaxTotal(totalSize);
		config.setMinIdle(totalSize);
		sessionFactory = getIoSessionFactory(configList, handler);
		IO_SESSION_POOL = new GenericObjectPool<IoSession>(sessionFactory, config);
		sessionFactory.init();
	}
	
	protected abstract IoSessionFactory getIoSessionFactory(List<MinaServerConfig> configList, IoHandlerAdapter handler);
	
	
	public static void addFailurServer(IoSession session){
		try {
			IO_SESSION_POOL.invalidateObject(session);
		} catch (Exception e) {
			sessionFactory.error("异常" + e);
		}
		Object sessionConfig = session.getAttribute(Constants.SESSION_CONFIG);
		if(null != sessionConfig){
			sessionFactory.addFailurServer(MinaServerConfig.class.cast(sessionConfig), false, false);
		}
		
	}
	public static IoSession getIoSession(){
		IoSession session = null;
		try{
			session = IO_SESSION_POOL.borrowObject();
		} catch(SessionCreateException e){
			return null;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		return session;
	}
	
	public static void returnIoSession(IoSession session){
		try{
			IO_SESSION_POOL.returnObject(session);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void addObject(){
		try {
			IO_SESSION_POOL.addObject();
		} catch (Exception e) {
			sessionFactory.error("异常" + e);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
	}
}
