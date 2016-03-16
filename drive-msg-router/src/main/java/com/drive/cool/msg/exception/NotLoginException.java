/**
 * kevin 2015年9月7日
 */
package com.drive.cool.msg.exception;

/**
 * @author kevin
 *
 */
public class NotLoginException extends RuntimeException{
	/**
	 * 
	 */
	public NotLoginException(String errorMsg) {
		super(errorMsg);
	}
}
