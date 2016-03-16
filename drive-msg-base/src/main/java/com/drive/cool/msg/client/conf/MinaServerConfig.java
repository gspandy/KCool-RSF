/**
 * kevin 2015年7月20日
 */
package com.drive.cool.msg.client.conf;

/**
 * @author kevin
 *
 */
public class MinaServerConfig {
	/**
	 * server的ip
	 */
	private String ip;
	/**
	 * server的端口
	 */
	private int port;
	
	/**
	 * session的个数，最大、最小都是这个
	 */
	private int size;
	
	/**
	 * 当前连接成功的数量
	 */
	private int successSize = 0;
	
	/**
	 * 当前连接失败的数量
	 */
	private int failurSize = 0;
	
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ip]=" + getIp() + " [port]=" + getPort() + " [successSize]=" + successSize + " [failureSize]=" + failurSize;
	}
	/**
	 * @return the successSize
	 */
	public int getSuccessSize() {
		return successSize;
	}
	
	/**
	 * @return the failurSize
	 */
	public int getFailurSize() {
		return failurSize;
	}
	
	
	/**
	 * 连接成功数量 +1
	 */
	public void increaseSuccessSize(){
		this.successSize++;
	}
	
	/**
	 * 连接失败数量 +1
	 */
	public void increaseFailurSize(){
		this.failurSize++;
	}
	
	/**
	 * 连接失败的重连成功
	 */
	public void failurToSuccess(){
		this.failurSize--;
		this.successSize++;
	}
	
	public void successToFailur(){
		this.failurSize++;
		this.successSize--;
	}

	public boolean isInit(){
		return this.failurSize + this.successSize < this.size;
	}
}
