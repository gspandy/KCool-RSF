/**
 * kevin 2015年9月7日
 */
package com.drive.cool.msg.client.session;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.drive.cool.message.pack.MessageDecoder;
import com.drive.cool.message.pack.MessageEncoder;
import com.drive.cool.message.pack.MessageRouteDecoder;
import com.drive.cool.message.pack.MessageRouteEncoder;
import com.drive.cool.msg.client.conf.MinaServerConfig;

/**
 * @author kevin
 *
 */
public class CommonIoSessionFactory extends IoSessionFactory {

	private final static Log log = LogFactory.getLog(CommonIoSessionFactory.class);
	

	public CommonIoSessionFactory(List<MinaServerConfig> configList,
			IoHandlerAdapter handler, boolean isAccess) {
		super(configList, handler, isAccess);
	}
	
	public boolean isDebug(){
		return log.isDebugEnabled();
	}
	public void debug(String logInfo){
		log.debug(logInfo);
	}
	public void error(String logInfo){
		log.error(logInfo);
	}
	
	/**
	 * 设置ssl相关信息
	 */
	protected void setSslFilter(NioSocketConnector connector){
		
	}
	
	protected  CumulativeProtocolDecoder getDecoder(){
		if(isAccess){
			return new MessageRouteDecoder();
		}else{
			return new MessageDecoder();
		}
	}
	
	protected ProtocolEncoderAdapter getEncoder(){
		if(isAccess){
			return new MessageRouteEncoder();
		}else{
			return new MessageEncoder();
		}
	}
}
