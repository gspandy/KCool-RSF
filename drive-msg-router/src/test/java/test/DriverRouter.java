/**
 * kevin 2015年7月25日
 */
package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author kevin
 *
 */
public class DriverRouter {
	private final static String CONTEXT_PATH = "classpath:spring/applicationContext-mina-router.xml";
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(CONTEXT_PATH);
		
	}
}
