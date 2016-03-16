/**
 * kevin 2015年8月4日
 */
package com.drive.cool.message.service.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.drive.cool.message.conf.ServiceConfig;
import com.drive.cool.message.dataset.IDataset;
import com.drive.cool.message.dataset.MapValueDataset;
import com.drive.cool.message.event.IEvent;
import com.drive.cool.message.service.IServiceFactory;
import com.drive.cool.message.util.EventFactory;
import com.drive.cool.rcp.IRcpService;
import com.drive.cool.tool.util.Constants;
/**
 * 服务自动注册
 * @author kevin
 *
 */
public class ServiceFactoryImpl implements IServiceFactory, InitializingBean, ApplicationContextAware{
	private static final int BASE_LENGTH = "com.drive.cool".length() + 1;
	private static ApplicationContext ctx;
	@Value("${service.factory.register.regx}")
	private String registerRegx;
	@Value("${service.factory.notregister.regx}")
	private String notRegisterRegx;
	
	private List<Pattern> registerPattern = new ArrayList<Pattern>();
	private List<Pattern> notRegisterPattern = new ArrayList<Pattern>();
	private boolean useRegisterPattern = false;
	private boolean useNotRegisterPattern = false;
	
	private static Map<String, ServiceConfig> ALL_SERVICE = new HashMap<String, ServiceConfig>();
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		initRegisterRegx();
		initServiceConfig();
		
	}

	private void initRegisterRegx(){
		if(StringUtils.isNotBlank(registerRegx)){
			useRegisterPattern = true;
			for(String regx : registerRegx.split(",")){
				registerPattern.add(Pattern.compile(regx));
			}
		}else {
			if(StringUtils.isNotBlank(notRegisterRegx)){
				useNotRegisterPattern = true;
				for(String regx : notRegisterRegx.split(",")){
					notRegisterPattern.add(Pattern.compile(regx));
				}
			}
		}
	}
	
	/**
	 * 初始化功能配置，从spring的bean里取所有加了注解的方法对外开放。<br>
	 * func为 beanId.funcId
	 */
	private void initServiceConfig() {
		Map<String, IRcpService> allService = this.ctx.getBeansOfType(IRcpService.class);
		for(String beanId : allService.keySet()){
			Object bean = allService.get(beanId);
			//如果不是自动生成的代理类，注册服务
			if(bean.getClass().isAssignableFrom(bean.getClass())){
				addOneClassMethod(beanId, bean);
			}
		}
	}

	/**
	 * @param beanId
	 * @param bean
	 */
	private void addOneClassMethod(String beanId, Object bean) {
		Class claz = AopUtils.getTargetClass(bean);
		if(!Proxy.class.isAssignableFrom(claz)){
			Method[] methods = claz.getDeclaredMethods();
			for(Method method : methods){
				addOneMethod(beanId, bean, method);
			}
		}
	}

	/**
	 * @param beanId
	 * @param bean
	 * @param method
	 */
	private void addOneMethod(String beanId, Object bean, Method method) {
		String methodName = method.getName();
		//去掉最前面的 com.drive.cool 节省点传输资源
		String param = getMethodParam(method);
		Class declaringClass = getInterface(method, bean, param);
		if(null == declaringClass) return;
		StringBuffer funcBuffer = new StringBuffer(declaringClass.getName().substring(BASE_LENGTH));
		funcBuffer.append(".").append(methodName);
		funcBuffer.append(param);
		String func = funcBuffer.toString();
		if(needRegisterCheck(func)){
			ServiceConfig config = new ServiceConfig();
			config.setBean(bean);
			config.setBeanId(beanId);
			config.setFunc(func);
			config.setMethod(methodName);
			ALL_SERVICE.put(func, config);
		}
	}
	
	private boolean needRegisterCheck(String func){
		boolean result = true;
		if(useRegisterPattern){
			result = false;
			for(Pattern pattern : registerPattern){
				if(isMatch(pattern, func)){
					result = true;
					break;
				}
			}
		}else if(useNotRegisterPattern){
			for(Pattern pattern : notRegisterPattern){
				if(isMatch(pattern, func)){
					result = false;
					break;
				}
			}
		}
		return result;
	}
	
	private static boolean isMatch(Pattern p,String sb){
		if(sb == null || sb.equals("")) return false;
		Matcher m = p.matcher(sb);
		if(m.find()){
			return true;
		}
		return false;
	}

	/**
	 * 根据方法获取参数
	 * @param method
	 * @return
	 */
	private String getMethodParam(Method method){
		Type[] parameterTypes = method.getGenericParameterTypes();
		StringBuffer funcParamBuffer = new StringBuffer();
		String simpleName;
		if(parameterTypes.length > 0){
			for(Type paramType : parameterTypes){
				if(paramType instanceof ParameterizedType){
					simpleName = Class.class.cast(ParameterizedType.class.cast(paramType).getRawType()).getSimpleName();
				}else{
					simpleName = Class.class.cast(paramType).getSimpleName();
				}
				funcParamBuffer.append(".").append(simpleName);
			}
		}
		return funcParamBuffer.toString();
	}
	
	/**
	 * 获取定义了该方法的接口
	 * @param method 方法
	 * @param bean 实现类
	 * @param param 参数
	 * @return
	 */
	private Class getInterface(Method method, Object bean, String param){
		Class[] allInterfaces = bean.getClass().getInterfaces();
		for(Class currInterface : allInterfaces){
			Method[] allMethod = currInterface.getDeclaredMethods();
			for(Method currMethod : allMethod){
				if(method.getName().equals(currMethod.getName())){
					String currParam = getMethodParam(currMethod);
					if(currParam.equals(param)){
						return currInterface;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据func获取配置
	 * @param func
	 * @return
	 */
	@Override
	public ServiceConfig getServiceConfig(String func){
		return ALL_SERVICE.get(func);
	}

	/* (non-Javadoc)
	 * @see com.drive.cool.message.service.IServiceFactory#getAllServiceConfig()
	 */
	@Override
	public Map<String, ServiceConfig> getAllServiceConfig() {
		return ALL_SERVICE;
	}

	@Override
	public void register(IoSession session, String sessionAlias){
		IEvent loginEvent = EventFactory.getNewRequest(IEvent.EVENT_REGISTER);
		IDataset funcDataset = new MapValueDataset();
		Map funcMap = new HashMap<String, Object>();
		Map<String, ServiceConfig> allService = getAllServiceConfig();
		for(String func : allService.keySet()){
			funcMap.put(func, null);
		}
		funcMap.put(Constants.SESSION_ALIAS, sessionAlias);
		funcDataset.setValue(funcMap);
		loginEvent.setDataset(funcDataset);
		session.write(loginEvent);
	}
	
}
