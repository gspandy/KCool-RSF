/**
 * kevin 2015年7月21日
 */
package com.drive.cool.message.conf;


/**
 * 定义功能与调用方法之间的关系
 * @author kevin
 *
 */
public class ServiceConfig {
	/**
	 * 调用的功能
	 */
	private String func;
	
	/**
	 * 对应的bean
	 */
	private String beanId;
	
	/**
	 * 调用的具体方法
	 */
	private String method;

	/**
	 * 从spring工程里获取的最终调用的bean
	 */
	private Object bean; 
	
	/**
	 * @return the func
	 */
	public String getFunc() {
		return func;
	}

	/**
	 * @param func the func to set
	 */
	public void setFunc(String func) {
		this.func = func;
	}

	/**
	 * @return the beanId
	 */
	public String getBeanId() {
		return beanId;
	}

	/**
	 * @param beanId the beanId to set
	 */
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}


	/**
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * @param bean the bean to set
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}
	
	
	
}
