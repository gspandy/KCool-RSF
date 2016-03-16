/**
 * kevin 2015年8月5日
 */
package com.drive.cool.msg.router;

import com.drive.cool.tool.util.TimeElapse;

/**
 * @author kevin
 *
 */
public class RouterTimeElapse {
	public static final String CALL_FUNC = "调用功能";
	private static TimeElapse ROUTER_ELAPSE = new TimeElapse("router");
	private static RouterTimeElapse elapse = null;
	
	private RouterTimeElapse() {
		ROUTER_ELAPSE.addTimeInfo(CALL_FUNC);
	}
	
	public synchronized static RouterTimeElapse getInstance() {
		if(null == elapse){
			elapse = new RouterTimeElapse();
		}
		return elapse;
	}
	
	public void calculate(String name, long startTime) {
		ROUTER_ELAPSE.calculate(name, startTime);
	}
}
