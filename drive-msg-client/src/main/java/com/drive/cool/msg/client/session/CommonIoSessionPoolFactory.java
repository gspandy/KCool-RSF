/**
 * kevin 2015年9月7日
 */
package com.drive.cool.msg.client.session;

import java.util.List;

import org.apache.mina.core.service.IoHandlerAdapter;

import com.drive.cool.msg.client.conf.MinaServerConfig;

/**
 * @author kevin
 *
 */
public class CommonIoSessionPoolFactory extends IoSessionPoolFactory {
	
	/**
	 * @param configList
	 * @param handler
	 */


	public CommonIoSessionPoolFactory(List<MinaServerConfig> configList,
			IoHandlerAdapter handler, boolean isAccess) {
		super(configList, handler, isAccess);
		
	}
	/* (non-Javadoc)
	 * @see com.drive.cool.msg.client.session.IoSessionPoolFactory#getIoSessionFactory(java.util.List, org.apache.mina.core.service.IoHandlerAdapter)
	 */
	@Override
	protected IoSessionFactory getIoSessionFactory(
			List<MinaServerConfig> configList, IoHandlerAdapter handler) {
		return new CommonIoSessionFactory(configList, handler, isAccess);
	}

	public boolean isAccess() {
		return isAccess;
	}

	public void setAccess(boolean isAccess) {
		this.isAccess = isAccess;
	}

}
