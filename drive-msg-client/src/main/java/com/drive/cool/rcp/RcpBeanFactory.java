/**
 * kevin 2015年8月7日
 */
package com.drive.cool.rcp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import com.drive.cool.rcp.aop.RcpInterceptor;

/**
 * 远程调用服务代理类生成
 * @author kevin
 *
 */
public class RcpBeanFactory implements BeanFactoryPostProcessor {

	private static Log log = LogFactory.getLog(RcpBeanFactory.class);
	private ResourcePatternResolver mappingFileResolver = new PathMatchingResourcePatternResolver();
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private MetadataReaderFactory metadataReaderFactory =
			new CachingMetadataReaderFactory(this.resourcePatternResolver);
	private InvocationHandler rcpInterceptor = null;
	private static char slash = '/';
	private static String SUFFIX = "/**/*.class";
	/**
	 * 扫描的基础包
	 */
	private String basePackage;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.rcpInterceptor = new RcpInterceptor(basePackage);
		//生成继承了 IRcpService接口的接口的代理类
		String basePackagePathComponent = basePackage.replace('.', slash);
		String path = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + 
				basePackagePathComponent + SUFFIX;
		Resource[] scannedResources = null;
		try {
			scannedResources = mappingFileResolver.getResources(path);
			for (Resource resource : scannedResources) {
				addOneBean(beanFactory, resource);
			}
		} catch (Exception e) {
			log.error("获取class失败");
			e.printStackTrace();
		}
		
	}

	/**
	 * 只对没有具体实现类的接口生成代理类
	 */
	private void addOneBean(ConfigurableListableBeanFactory beanFactory,
			Resource resource){
		if (resource.isReadable()){
			try{
				MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
				String className = metadataReader.getAnnotationMetadata().getClassName();
				Class clazz = Class.forName(className);
				if(IRcpService.class.isAssignableFrom(clazz) 
						&& clazz.isInterface()){
					String simpleName = clazz.getSimpleName(); 
					//beanId的命名规则为去掉接口前面的I，然后剩下的字母里第二个字母小写
					String beanId = simpleName.substring(1,2).toLowerCase() + simpleName.substring(2);
					if(beanFactory.containsBean(beanId)){
						return;
					}
					Class proxyClass = Proxy.getProxyClass(clazz.getClassLoader(), new Class[] { clazz });
					Object bean = proxyClass.getConstructor(new Class[] { InvocationHandler.class })
							.newInstance(new Object[] { this.rcpInterceptor });
					beanFactory.registerSingleton(beanId, bean);
					
				}
			}catch(Exception e){
				log.error("实例化bean失败" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the basePackage
	 */
	public String getBasePackage() {
		return basePackage;
	}

	/**
	 * @param basePackage the basePackage to set
	 */
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}
}
