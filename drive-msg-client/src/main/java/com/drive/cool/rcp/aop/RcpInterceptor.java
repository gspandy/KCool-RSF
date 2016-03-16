/**
 * kevin 2015年7月27日
 */
package com.drive.cool.rcp.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.drive.cool.message.dataset.IDataset;
import com.drive.cool.message.dataset.MultiObjectDataset;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.msg.client.MinaClient;
import com.drive.cool.rcp.IRcpService;

/**
 * @author kevin
 *
 */
public class RcpInterceptor implements InvocationHandler {
	private static int BASE_LENGTH ;
	/**
	 * 
	 */
	public RcpInterceptor(String basePackage) {
		BASE_LENGTH = basePackage.length() + 1;
	}

	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		//进入到这里的，说明是需要调用远程实现功能的
		Class superInterface = method.getDeclaringClass();
		//String simpleName = superInterface.getSimpleName();
		//String beanId = simpleName.substring(1,2).toLowerCase() + simpleName.substring(2);
		String funcBase = superInterface.getName();
		String methodName = method.getName();
		Type[] parameterTypes = method.getGenericParameterTypes();
		StringBuffer funcBuffer = new StringBuffer(funcBase.substring(BASE_LENGTH));
		funcBuffer.append(".").append(methodName);
		String simpleName;
		if(parameterTypes.length > 0){
			for(Type paramType : parameterTypes){
				if(paramType instanceof ParameterizedType){
					simpleName = Class.class.cast(ParameterizedType.class.cast(paramType).getRawType()).getSimpleName();
				}else{
					simpleName = Class.class.cast(paramType).getSimpleName();
				}
				funcBuffer.append(".").append(simpleName);
			}
		}
		String func = funcBuffer.toString();
		IEvent event = EventFactory.getNewRequest(IEvent.EVENT_NORMAL, func);
		MultiObjectDataset dataset = new MultiObjectDataset();
		dataset.setValue(args);
		event.setDataset(dataset);
		IEvent result = null;
		result = MinaClient.getMinaClient().postMessage(event);
		if(null == result || 0 != result.getErrorNo()){
			String errorMsg = "执行功能出错";
			if(null != result) errorMsg = result.getErrorInfo();
			throw new RuntimeException(errorMsg);
		}
		IDataset resultDataset = result.getDataset();
		Object returnValue = (null == resultDataset) ? null : resultDataset.getValue();
		return returnValue;
	}

	public static void main(String[] args) {
		System.out.println(IRcpService.class.getName());
	}
}
