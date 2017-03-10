package com.ctg.itrdc.janus.container.jetty;

import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.container.Container;
import com.ctg.itrdc.janus.container.page.PageServlet;
import com.ctg.itrdc.janus.container.page.ResourceFilter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Jetty容器
 * 
 * @author Administrator
 */
public class JettyContainer implements Container {

	private static final Logger logger = LoggerFactory
			.getLogger(JettyContainer.class);

	/**
	 * jetty端口
	 */
	public static final String JETTY_PORT = "dubbo.jetty.port";

	/**
	 * jetty目录
	 */
	public static final String JETTY_DIRECTORY = "dubbo.jetty.directory";

	public static final String JETTY_PAGES = "dubbo.jetty.page";

	/**
	 * 缺省端口
	 */
	public static final int DEFAULT_JETTY_PORT = 8080;

	/**
	 * jetty的内置连接器
	 */
	SelectChannelConnector connector;

	/**
	 * 启动jetty容器
	 */
	public void start() {
		int port = initConnector();
		ServletHandler handler = initServletHandler();
		Server server = new Server();
		server.addConnector(connector);
		server.addHandler(handler);
		try {
			server.start();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to start jetty server on "
					+ NetUtils.getLocalHost() + ":" + port + ", cause: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * 初始化ServletHandler
	 * 
	 * @return
	 */
	private ServletHandler initServletHandler() {
		ServletHandler handler = new ServletHandler();
		String resources = ConfigUtils.getProperty(JETTY_DIRECTORY);
		if (resources != null && resources.length() > 0) {
			FilterHolder resourceHolder = handler.addFilterWithMapping(
					ResourceFilter.class, "/*", Handler.DEFAULT);
			resourceHolder.setInitParameter("resources", resources);
		}

		ServletHolder pageHolder = handler.addServletWithMapping(
				PageServlet.class, "/*");
		pageHolder.setInitParameter("pages",
				ConfigUtils.getProperty(JETTY_PAGES));
		pageHolder.setInitOrder(2);
		return handler;
	}

	/**
	 * 初始化jetty的connector
	 * 
	 * @return
	 */
	private int initConnector() {
		String serverPort = ConfigUtils.getProperty(JETTY_PORT);
		int port;
		if (serverPort == null || serverPort.length() == 0) {
			port = DEFAULT_JETTY_PORT;
		} else {
			port = Integer.parseInt(serverPort);
		}
		connector = new SelectChannelConnector();
		connector.setPort(port);
		return port;
	}

	/**
	 * 停止容器
	 */
	public void stop() {
		try {
			if (connector != null) {
				connector.close();
				connector = null;
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}