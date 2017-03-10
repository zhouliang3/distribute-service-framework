package com.ctg.itrdc.janus.container.spring;

import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.container.Container;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Spring容器
 * 
 * @author Administrator
 */
public class SpringContainer implements Container {

	private static final Logger logger = LoggerFactory
			.getLogger(SpringContainer.class);

	public static final String SPRING_CONFIG = "dubbo.spring.config";//iundo 此处需要修改

	public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

	static ClassPathXmlApplicationContext context;

	public static ClassPathXmlApplicationContext getContext() {
		return context;
	}

	/**
	 * 启动spring容器
	 */
	public void start() {
		String configPath = ConfigUtils.getProperty(SPRING_CONFIG);
		if (configPath == null || configPath.length() == 0) {
			configPath = DEFAULT_SPRING_CONFIG;
		}
		context = new ClassPathXmlApplicationContext(
				configPath.split("[,\\s]+"));
		context.start();
	}

	/**
	 * 停止spring容器
	 */
	public void stop() {
		try {
			if (context != null) {
				context.stop();
				context.close();
				context = null;
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

}