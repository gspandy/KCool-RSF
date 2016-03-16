/**
 * kevin 2015年9月6日
 */
package com.drive.cool.msg.client.session;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.drive.cool.message.handler.RegisterHandler;
import com.drive.cool.message.pack.MessageDecoder;
import com.drive.cool.message.pack.MessageEncoder;
import com.drive.cool.msg.client.conf.MinaServerConfig;
import com.drive.cool.msg.client.exception.SessionCreateException;
import com.drive.cool.msg.codec.MessageCodecFactory;
import com.drive.cool.tool.util.Constants;

/**
 * @author kevin
 *
 */
public class IoSessionFactory extends BasePooledObjectFactory<IoSession>{

	public static String SESSION_ALIAS = UUID.randomUUID().toString().replaceAll("-", "");
	
	private Object lock = new Object();
	Object intervalObject = new Object();
	private List<MinaServerConfig> configList;
	private IoHandlerAdapter handler;
	private int failureSize = 0;
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	private static int SLEEP_TIME = 5000;
	//是否接入端drive-msg-access使用
	protected boolean isAccess = false;
	public boolean isDebug(){
		return false;
	}
	public void debug(String logInfo){
		
	}
	public void error(String logInfo){
		
	}
	
	/**
	 * 设置ssl相关信息
	 */
	protected void setSslFilter(NioSocketConnector connector){
		
	}
	/**
	 * 添加连接失败的配置（可能是未连接成功，也可能是中间断开）<br>
	 * 失败后需要重连，因此唤醒重连进程<br>
	 * @param config
	 * @param isRetry 是否重连时连接失败
	 */
	public void addFailurServer(MinaServerConfig config, boolean isRetry, boolean isInit){
		synchronized(lock){
			if(isInit){
				config.increaseFailurSize();
				this.failureSize++;
			}else if(!isRetry) {
				config.successToFailur();
				this.failureSize++;
			}
			if(isDebug()) debug("连接断开:[failureSize]=" + this.failureSize + "[isRetry]=" + isRetry + "[isInit]="+  isInit + " " + config);
		}
	}
	
	public void init(){

		for(MinaServerConfig config : configList){
			for(int i=0; i<config.getSize();i++){
    			try {
    				IoSessionPoolFactory.addObject();
				} catch (Exception e) {
					error("创建连接失败：" + config);
				}
    		}
		}
		new Thread(connectRunnable).start();
	}
	private Runnable connectRunnable = new Runnable() {
		@Override
		public void run() {
			while (isRunning.get()) {
				try{
					int times = 0;
					int currFailureSize = failureSize;
					while (times < currFailureSize && failureSize > 0) {
						IoSessionPoolFactory.addObject();
						Thread.sleep(0);
						times++;
					}
					synchronized (intervalObject) {
						intervalObject.wait(SLEEP_TIME);
					}
				}catch(Exception e){
					error("连接异常" + e);
				}
			}
		}
	};
	
	
	/**
	 * 
	 */
	public IoSessionFactory(List<MinaServerConfig> configList, IoHandlerAdapter handler,
			boolean isAccess) {
		this.isAccess = isAccess;
		initConfig(configList, handler);
	}
	/**
	 * @param configList
	 * @param handler
	 */
	private void initConfig(List<MinaServerConfig> configList,
			IoHandlerAdapter handler) {
		this.configList = configList;
		this.handler = handler;
		this.isRunning.set(true);
	}
	/* (non-Javadoc)
	 * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
	 */
	@Override
	public IoSession create() throws Exception {
		//创建连接，目前不对之前断开过的连接做优先（滞后）处理，直接按顺序取需要连接的配置进行连接
		IoSession session;
		int retryTime = 0;
		synchronized (lock) {
			while(retryTime < configList.size()){
				//当前取到进行连接的是不是之前连接失败的连接
				boolean isRetry = false;
				MinaServerConfig config = getConfig();
				if(null == config){ //说明所有连接都已经创建好
					return null;
				}
				boolean isInit = config.isInit();
					
				if(config.getFailurSize() > 0){
					isRetry = true;
				}
				
				NioSocketConnector connector = getConnect(config);
				ConnectFuture cf = connector.connect(new InetSocketAddress(config.getIp(),config.getPort()));
		    	cf.awaitUninterruptibly();
		    	if(!cf.isConnected()){
		    		//连接失败，当前配置移到最后面 换一个服务器重试
		    		moveToLast(config);
		    		addFailurServer(config, isRetry, isInit);
		    		error("创建连接时连接服务器失败:" + config);
		    		retryTime++;
		    		connector.dispose(false);
		    		continue;
		    	}
		    	session = cf.getSession();
		    	session.setAttribute(Constants.SESSION_CONNECTOR, connector);
				if(session.isConnected()){
					session.setAttribute(Constants.SESSION_ALIAS, SESSION_ALIAS);
					RegisterHandler.class.cast(handler).register(session);
					if(isRetry){
						this.failureSize--;
						config.failurToSuccess();
					}else{
						config.increaseSuccessSize();
					}
					if(isDebug()) debug("连接成功：[failurSize]=" + this.failureSize + "[isRetry]=" + isRetry + "[isInit]="+  isInit + " " + config);
					session.setAttribute(Constants.SESSION_CONFIG, config);
					return session;
				}else{
					session.close(true);
					moveToLast(config);
		    		addFailurServer(config, isRetry, isInit);
		    		error("创建连接时连接服务器失败:" + config);
		    		retryTime++;
		    		continue;
				}
			}
			return null;
		}
	}
	
	/**
	 * 从所有连接配置里取一个需要连接的配置
	 * @return
	 */
	private MinaServerConfig getConfig(){
		if(0 == this.configList.size()){
			return null;
		}
		int length = this.configList.size();
		int tryTime = 0;
		MinaServerConfig config = this.configList.get(0);
		while(config.getSuccessSize() >= config.getSize()){
			//把不符合要求的config移到最后面
			moveToLast(config);
			tryTime++;
			if(tryTime == length){
				return null;
			}
			config = this.configList.get(0);
		}
		return config;
	}
	/**
	 * @param config
	 */
	private void moveToLast(MinaServerConfig config) {
		this.configList.remove(0);
		this.configList.add(config);
	}
	
	protected  CumulativeProtocolDecoder getDecoder(){
		return new MessageDecoder();
	}
	
	protected ProtocolEncoderAdapter getEncoder(){
		return new MessageEncoder();
	}
	
	

	private NioSocketConnector getConnect(MinaServerConfig config) {
		NioSocketConnector connector = new NioSocketConnector();	
		connector.setDefaultRemoteAddress(InetSocketAddress.createUnresolved(config.getIp(), config.getPort()));
		int idleTime = 600;
		connector.getSessionConfig().setWriterIdleTime(idleTime);
		connector.getSessionConfig().setReaderIdleTime(idleTime);
		connector.getSessionConfig().setBothIdleTime(idleTime);
		
		SocketSessionConfig.class.cast(connector.getSessionConfig()).setTcpNoDelay(true);
		
		setSslFilter(connector);
		setLogFilter(connector);
		// 打包 解包
		ProtocolCodecFilter protocolCodecFileter = new ProtocolCodecFilter(getProtocolFactory());
		connector.getFilterChain().addLast("codec", protocolCodecFileter);
			
	    connector.setHandler(handler);
	    
	    // 心跳包部分
		KeepAliveRequestTimeoutHandler keepAliveHandler = new KeepAliveHandler();
		KeepAliveFilter keepAliveFilter = new KeepAliveFilter(new KeepAliveFactory(isAccess), IdleStatus.READER_IDLE);
		keepAliveFilter.setForwardEvent(false);
		keepAliveFilter.setRequestInterval(5);
		keepAliveFilter.setRequestTimeoutHandler(keepAliveHandler);
		keepAliveFilter.setRequestTimeout(60);
		connector.getFilterChain().addLast("keepAliveFilter", keepAliveFilter);
	 		
		connector.getFilterChain().addLast("ThreadPool",new ExecutorFilter(Executors.newCachedThreadPool()));
		
	    SocketSessionConfig socketSessionConfig = (SocketSessionConfig) connector.getSessionConfig();
		socketSessionConfig.setTcpNoDelay(true);
		socketSessionConfig.setMaxReadBufferSize(8 * 1024);
		socketSessionConfig.setKeepAlive(true);
		socketSessionConfig.setReceiveBufferSize(8 * 1024);
		socketSessionConfig.setSendBufferSize(8 * 1024);
		connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 20);
		return connector;
	}
	/**
	 * @return
	 */
	protected ProtocolCodecFactory getProtocolFactory() {
		return new MessageCodecFactory(getDecoder(), getEncoder());
	}
	protected void setLogFilter(NioSocketConnector connector) {
		// 设置日志
		LoggingFilter loggingFilter = new LoggingFilter();
		loggingFilter.setExceptionCaughtLogLevel(LogLevel.INFO);
		loggingFilter.setMessageReceivedLogLevel(LogLevel.INFO);
		loggingFilter.setMessageSentLogLevel(LogLevel.INFO);
		loggingFilter.setSessionClosedLogLevel(LogLevel.INFO);
		loggingFilter.setSessionCreatedLogLevel(LogLevel.INFO);
		loggingFilter.setSessionOpenedLogLevel(LogLevel.INFO);
		loggingFilter.setSessionIdleLogLevel(LogLevel.INFO);
		connector.getFilterChain().addLast("loggingFilter", loggingFilter);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
	 */
	@Override
	public PooledObject<IoSession> wrap(IoSession obj) {
		if(null == obj) throw new SessionCreateException("创建session失败。");
		return new DefaultPooledObject<IoSession>(obj);
	}

}
