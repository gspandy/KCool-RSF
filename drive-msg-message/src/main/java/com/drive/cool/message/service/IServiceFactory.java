/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.service;

import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.drive.cool.message.conf.ServiceConfig;

/**
 * 服务注册接口
 * @author kevin
 *
 */
public interface IServiceFactory{

	
	/**
	 * 根据func获取配置
	 * @param func
	 * @return
	 */
	public ServiceConfig getServiceConfig(String func);
	
	/**
	 * 获取所有的service
	 */
	public  Map<String, ServiceConfig> getAllServiceConfig();

	/**
	 * 注册功能到路由
	 * @param session
	 */
	public void register(IoSession session, String sessionAlias);
}
