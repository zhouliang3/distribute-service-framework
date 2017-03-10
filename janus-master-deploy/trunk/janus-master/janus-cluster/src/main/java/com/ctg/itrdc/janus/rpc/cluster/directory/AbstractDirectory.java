package com.ctg.itrdc.janus.rpc.cluster.directory;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.Directory;
import com.ctg.itrdc.janus.rpc.cluster.Router;
import com.ctg.itrdc.janus.rpc.cluster.RouterFactory;
import com.ctg.itrdc.janus.rpc.cluster.router.MockInvokersSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 增加router的Directory
 * 
 * @author Administrator
 */
public abstract class AbstractDirectory<T> implements Directory<T> {

	// 日志输出
	private static final Logger logger = LoggerFactory
			.getLogger(AbstractDirectory.class);

	private final URL url;

	private volatile boolean destroyed = false;

	private volatile URL consumerUrl;

	/**
	 * 路由规则列表
	 */
	private volatile List<Router> routers;

	public AbstractDirectory(URL url) {
		this(url, null);
	}

	public AbstractDirectory(URL url, List<Router> routers) {
		this(url, url, routers);
	}

	public AbstractDirectory(URL url, URL consumerUrl, List<Router> routers) {
		if (url == null)
			throw new IllegalArgumentException("url == null");
		this.url = url;
		this.consumerUrl = consumerUrl;
		setRouters(routers);
	}

	/**
	 * 查找Invoker列表
	 * 
	 * 如果当前已经销毁，抛RpcException异常;<br/>
	 * 如果本地的routers不为空，将执行路由，得到新的invokes
	 */
	public List<Invoker<T>> list(Invocation invocation) throws RpcException {
		if (destroyed) {
			throw new RpcException("Directory already destroyed .url: "
					+ getUrl());
		}
		List<Invoker<T>> invokers = doList(invocation);
		List<Router> localRouters = this.routers; // local reference
		if (localRouters == null || localRouters.size() == 0) {
			return invokers;
		}
		for (Router router : localRouters) {
			try {
				if (router.getUrl() == null
						|| router.getUrl().getParameter(Constants.RUNTIME_KEY,
								true)) {
					invokers = router.route(invokers, getConsumerUrl(),
							invocation);
				}
			} catch (Throwable t) {
				logger.error("Failed to execute router: " + getUrl()
						+ ", cause: " + t.getMessage(), t);
			}
		}
		return invokers;
	}

	public URL getUrl() {
		return url;
	}

	public List<Router> getRouters() {
		return routers;
	}

	public URL getConsumerUrl() {
		return consumerUrl;
	}

	public void setConsumerUrl(URL consumerUrl) {
		this.consumerUrl = consumerUrl;
	}

	protected void setRouters(List<Router> routers) {
		// copy list
		routers = routers == null ? new ArrayList<Router>()
				: new ArrayList<Router>(routers);
		// append url router
		String routerkey = url.getParameter(Constants.ROUTER_KEY);
		if (routerkey != null && routerkey.length() > 0) {
			RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(
					RouterFactory.class).getExtension(routerkey);
			routers.add(routerFactory.getRouter(url));
		}
		// append mock invoker selector
		routers.add(new MockInvokersSelector());
		Collections.sort(routers);
		this.routers = routers;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void destroy() {
		destroyed = true;
	}

	protected abstract List<Invoker<T>> doList(Invocation invocation)
			throws RpcException;

}