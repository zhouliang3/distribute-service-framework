/*
 * Copyright 2016-2017 CHINA TELECOM GROUP.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ctg.itrdc.janus.container.log4j;

import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.container.Container;
import org.apache.log4j.*;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Log4jContainer. (SPI, Singleton, ThreadSafe)
 * 
 *
 */
public class Log4jContainer implements Container {

	public static final String LOG4J_FILE = "janus.log4j.file";

	public static final String LOG4J_LEVEL = "janus.log4j.level";

	public static final String LOG4J_SUBDIRECTORY = "janus.log4j.subdirectory";

	public static final String DEFAULT_LOG4J_LEVEL = "ERROR";

	@SuppressWarnings("unchecked")
	public void start() {
		String file = ConfigUtils.getProperty(LOG4J_FILE);
		if (StringUtils.isNotEmpty(file)) {
			configueProperties(file);
		}
		String subdirectory = ConfigUtils.getProperty(LOG4J_SUBDIRECTORY);
		if (subdirectory == null || subdirectory.length() == 0) {
			return;
		}
		Enumeration<Logger> ls = LogManager.getCurrentLoggers();
		while (LogManager.getCurrentLoggers().hasMoreElements()) {
			org.apache.log4j.Logger logger = ls.nextElement();
			if (logger != null) {
				initLoggerProperty(subdirectory, logger);
			}

		}
	}

	/**
	 * @param subdirectory
	 * @param logger
	 * @return
	 */
	private Enumeration<Appender> initLoggerProperty(String subdirectory,
			Logger logger) {
		Enumeration<Appender> as = logger.getAllAppenders();
		while (as.hasMoreElements()) {
			Appender a = as.nextElement();
			if (!(a instanceof FileAppender)) {
				continue;
			}
			FileAppender fa = (FileAppender) a;
			String f = fa.getFile();
			if (f != null && f.length() > 0) {
				String absoluteFileName = getAbsoluteFileName(subdirectory, f);
				fa.setFile(absoluteFileName);
				fa.activateOptions();
			}
		}
		return as;
	}

	/**
	 * 初始化log4j的配置属性
	 * 
	 * @param file
	 */
	private void configueProperties(String file) {
		String level = ConfigUtils.getProperty(LOG4J_LEVEL);
		if (level == null || level.length() == 0) {
			level = DEFAULT_LOG4J_LEVEL;
		}
		Properties properties = new Properties();
		properties.setProperty("log4j.rootLogger", level + ",application");
		properties.setProperty("log4j.appender.application",
				"org.apache.log4j.DailyRollingFileAppender");
		properties.setProperty("log4j.appender.application.File", file);
		properties.setProperty("log4j.appender.application.Append", "true");
		properties.setProperty("log4j.appender.application.DatePattern",
				"'.'yyyy-MM-dd");
		properties.setProperty("log4j.appender.application.layout",
				"org.apache.log4j.PatternLayout");
		properties.setProperty(
				"log4j.appender.application.layout.ConversionPattern",
				"%d [%t] %-5p %C{6} (%F:%L) - %m%n");
		PropertyConfigurator.configure(properties);
	}

	/**
	 * 根据子目录以及文件路径，合并最终的目标路径
	 * 
	 * @param subdirectory
	 * @param fileName
	 * @return
	 */
	private String getAbsoluteFileName(String subdirectory, String fileName) {
		int i = fileName.replace('\\', '/').lastIndexOf('/');
		String path;
		if (i == -1) {
			path = subdirectory;
		} else {
			path = fileName.substring(0, i);
			if (!path.endsWith(subdirectory)) {
				path = path + "/" + subdirectory;
			}
			fileName = fileName.substring(i + 1);
		}
		return path + "/" + fileName;
	}

	public void stop() {
	}

}