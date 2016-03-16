/**
 * kevin 2015年8月30日
 */
package com.drive.cool.msg.client.exception;

/**
 * @author kevin
 *
 */
public class NoIdleSessionException extends RuntimeException {
	/**
	 * 
	 */
	public NoIdleSessionException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 
	 */
	public NoIdleSessionException(String message) {
		super(message);
	}
	
}
