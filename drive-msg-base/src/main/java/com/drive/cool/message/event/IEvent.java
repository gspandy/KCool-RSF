/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.event;

import com.drive.cool.message.dataset.IDataset;

/**
 * 事件接口，所有数据包装成事件进行传输
 * @author kevin
 *
 */
public interface IEvent {
	public static final char EVENT_HEARTBEAT = 0x01;
	/**
	 * service向路由注册使用
	 */
	public static final char EVENT_REGISTER = 0x02;
	
	public static final char EVENT_NORMAL = 0x03;
	
	public static final char EVENT_SEND = 0x01;
	public static final char EVENT_RECEIVE = 0x02;
	
	
	public static final int DEFAULT_TIMEOUT = 300;
	
	/**
	 * 获取发送类型
	 * @return
	 */
	public char getSendType();
	/**
	 * 获取event类型
	 * @return
	 */
	public char getEventType();
	
	
	/**
	 * 获取event对应的功能
	 * @return
	 */
	public String getFunc();
	/**
	 * 获取event的内容
	 * @return
	 */
	public IDataset getDataset();
	
	/**
	 * 获取event的id
	 * @return
	 */
	public int getEventId();
	
	/**
	 * 设置event的内容
	 * @param dataset
	 */
	public void setDataset(IDataset dataset);
	
	/**
	 * 获取错误编号，为0时表示正常返回
	 * @return
	 */
	public int getErrorNo();
	
	/**
	 * 获取错误信息
	 * @return
	 */
	public String getErrorInfo();
	
	/**
	 * 设置错误号
	 * @param errorNo
	 */
	public void setErrorNo(int errorNo);
	
	/**
	 * 设置错误信息
	 * @param errorInfo
	 */
	public void setErrorInfo(String errorInfo);
	
	/**
	 * 设置调用的功能
	 * @param func
	 */
	public void setFunc(String func);
	
	/**
	 * 设置event的唯一id
	 * @param eventId
	 */
	public void setEventId(int eventId);
	
	/**
	 * 设置event发送类型
	 * @param sendType
	 */
	public void setSendType(char sendType);
	
	/**
	 * 设置超时时间，单位为秒
	 * @param timeout
	 */
	public void setTimeout(int timeout);
	
	/**
	 * 获取超时时间，单位为秒
	 * @return
	 */
	public int getTimeout();
	
	public void setEventType(char eventType);
}
