/**
 * kevin 2015年8月1日
 */
package com.drive.cool.message.event;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import com.drive.cool.message.dataset.IDataset;
import com.drive.cool.message.dataset.ListValueDataset;
import com.drive.cool.message.dataset.MapValueDataset;
import com.drive.cool.message.dataset.SimpleObjectDataset;
import com.drive.cool.message.util.BaseKryoPool;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * 事件
 * @author kevin
 *
 */
public class Event implements IEvent, IEventError, Serializable {
	
	private transient static AtomicInteger EVENT_ID = new AtomicInteger(0);
	/**
	 * 超时时长，单位为秒
	 */
	private int timeoutSecond = 0;
	/**
	 * event类型,normal,heartbeat,login
	 */
	protected char eventType;
	
	/**
	 * event的唯一id，只针对当前的client唯一
	 */
	private int eventId;
	
	/**
	 * event调用的功能
	 */
	private String func;
	
	/**
	 * 登陆的用户，对于需要登陆才能调用的功能适用，比如走ssl接口的功能
	 */
	private String userId;
	
	private byte[] messageBody;
	
	/**
	 * 错误号
	 */
	private int errorNo = 0;
	
	/**
	 * 错误信息
	 */
	private String errorInfo;
	
	/**
	 * event发送类型
	 */
	private char sendType;
	
	/**
	 * dataset的类型
	 */
	private char datasetType = IDataset.NULL_VALUE;
	
	/**
	 * 
	 */
	public Event() {
		
	}
	

	/**
	 * @param eventType the eventType to set
	 */
	@Override
	public void setEventType(char eventType) {
		this.eventType = eventType;
	}

	/**
	 * @param messageBody the messageBody to set
	 */
	public void setMessageBody(byte[] messageBody) {
		this.messageBody = messageBody;
	}

	public Event(char eventType){
		this(eventType, null);
	}
	public Event(char eventType, String func) {
		this.eventType = eventType;
		this.eventId = getNextEventId();
		setFunc(func);
	}
	
	public static int getNextEventId(){
		return EVENT_ID.incrementAndGet();
	}
	public Event(int eventId, char eventType){
		this.eventType = eventType;
		this.eventId = eventId;
	}
	
	@Override
	public char getEventType() {
		return this.eventType;
	}

	@Override
	public String getFunc() {
		return this.func;
	}

	/**
	 * 使用此方法注意，每一次都需要反序列化。需要多次使用时先用临时变量保存<br>
	 */
	@Override
	public IDataset getDataset() {
		if(null == messageBody){
			return null;
		}
		Input input = new Input(messageBody);
		Kryo kryo = null;
		IDataset dataset = null;
		try {
			kryo = BaseKryoPool.getInstance().getKryoPool().borrowObject();
			if(IDataset.MULTI_OBJECT == this.getDatasetType()){
				dataset = (IDataset) kryo.readClassAndObject(input);
			}else{
				Class clazz = getDatasetClazz();
				dataset = (IDataset) kryo.readObject(input, clazz);
			}
		} catch (Exception e) {
			throw new RuntimeException("反序列化失败", e);
		}finally{
			if (null != kryo) {
				try {
					BaseKryoPool.getInstance().getKryoPool().returnObject(kryo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return dataset;
	}
	/**
	 * 
	 */
	private Class getDatasetClazz() {
		Class clazz = null;
		if(IDataset.SIMPLE_OBJECT == this.datasetType){
			clazz = SimpleObjectDataset.class;
		}else if(IDataset.MAP_VALUE == this.datasetType) {
			clazz = MapValueDataset.class;
		}else if(IDataset.LIST_MAP_VALUE == this.datasetType){
			clazz = ListValueDataset.class;
		}
		return clazz;
	}

	@Override
	public int getEventId() {
		return this.eventId;
	}

	@Override
	public void setDataset(IDataset dataset)  {
		if(null == dataset) return;
		this.datasetType = dataset.getType();
		
		ByteArrayOutputStream outStream = null;
		Output output = null;
		Kryo kryo = null;
		try {
			kryo = BaseKryoPool.getInstance().getKryoPool().borrowObject();
			outStream = new ByteArrayOutputStream();
			output = new Output(outStream);
			//复杂对象序列化的时候把类名一起
			if(IDataset.MULTI_OBJECT == this.datasetType){
				kryo.writeClassAndObject(output, dataset);
			}else{
				kryo.writeObject(output, dataset);
			}
			output.flush();
			this.messageBody = outStream.toByteArray();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (null != kryo) {
				try {
					BaseKryoPool.getInstance().getKryoPool().returnObject(kryo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(null != outStream){
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null != output){
				output.close();
			}
		}
	}

	@Override
	public int getErrorNo() {
		return this.errorNo;
	}

	@Override
	public String getErrorInfo() {
		return this.errorInfo;
	}

	@Override
	public void setErrorNo(int errorNo){
		this.errorNo = errorNo;
	}
	
	@Override
	public void setErrorInfo(String errorInfo){
		if(null != errorInfo && errorInfo.length() > 4000){
			errorInfo = errorInfo.substring(0, 4000);
		}
		this.errorInfo = errorInfo;
		if(0 == this.errorNo && null != errorInfo){
			this.errorNo = -1;
		}
	}
	
	
	@Override
	public void setFunc(String func){
		if(null != func){
			int length = func.getBytes().length;
			if(length > 200){
				throw new RuntimeException("调用功能长度不能超过200。[func]=" + func);
			}
		}
		this.func = func;
	}
	
	@Override
	public void setEventId(int eventId){
		this.eventId = eventId;
	}
	
	@Override
	public char getSendType() {
		return sendType;
	}

	@Override
	public void setSendType(char sendType) {
		this.sendType = sendType;
	}
	/**
	 * @return the datasetType
	 */
	public char getDatasetType() {
		return datasetType;
	}
	/**
	 * @param datasetType the datasetType to set
	 */
	public void setDatasetType(char datasetType) {
		this.datasetType = datasetType;
	}
	
	/**
	 * 是否普通event
	 * @return
	 */
	public boolean isNormal(){
		return EVENT_NORMAL == this.eventType;
	}
	
	/**
	 * 是否心跳event
	 * @return
	 */
	public boolean isHeartbeat(){
		return EVENT_HEARTBEAT == this.eventType;
	}
	
	/**
	 * 是否登录login
	 * @return
	 */
	public boolean isLogin(){
		return EVENT_REGISTER == this.eventType;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer eventBuffer = new StringBuffer();
		if(isNormal()){
			eventBuffer.append("normal ");
		}else if(isLogin()){
			eventBuffer.append("login ");
		}else if(isHeartbeat()){
			eventBuffer.append("heartbeat ");
		}
		eventBuffer.append("id:").append(getEventId()).append(" ");
		eventBuffer.append("func:").append(getFunc()).append(" ");
		eventBuffer.append("userId:").append(getUserId()).append(" ");
		return eventBuffer.toString();
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEvent#setTimeout(int)
	 */
	@Override
	public void setTimeout(int timeout) {
		this.timeoutSecond = timeout;
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.event.IEvent#getTimeout()
	 */
	@Override
	public int getTimeout() {
		return 0 == this.timeoutSecond ? DEFAULT_TIMEOUT : this.timeoutSecond;
	} 
	
	public boolean isRequest(){
		return EVENT_SEND == this.sendType;
	}
	
	public boolean isResponse(){
		return EVENT_RECEIVE == this.sendType;
	}
	
	public boolean isMap(){
		return IDataset.MAP_VALUE == this.datasetType;
	}
	
	public boolean isList(){
		return IDataset.LIST_MAP_VALUE == this.datasetType;
	}
	public byte[] getMessageBody(){
		return this.messageBody;
	}


	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}


	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public boolean isSuccess(){
		return 0 == getErrorNo();
	}
}
