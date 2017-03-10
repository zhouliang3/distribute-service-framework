package com.ctg.itrdc.janus.rpc.protocol;

import java.util.List;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.rpc.Exporter;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Protocol;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * 过滤器Protocol，处理系统的调用过滤链filter。根据系统注册的filter，进行filter的调用处理。
 * 
 * 将filter按照级别的优先排列起来，级别高的先调用，级别低的后调用，最后是invoker本身的调用。
 * 
 * filter(highest order)->filter(high order)....->filter(low order)...->invoker
 * 
 * filter链处理分两种：<br/>
 * 1. export时,通过查找group为provider的filter链 <br/>
 * 2. refer时，通过查找group为consumer的filter链
 * 
 * @author Administrator
 */
public class ProtocolFilterWrapper implements Protocol {

	private final Protocol protocol;

	public ProtocolFilterWrapper(Protocol protocol) {
		if (protocol == null) {
			throw new IllegalArgumentException("protocol == null");
		}
		this.protocol = protocol;
	}

	public int getDefaultPort() {
		return protocol.getDefaultPort();
	}

	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
			return protocol.export(invoker);
		}
		return protocol.export(buildInvokerChain(invoker,
				Constants.SERVICE_FILTER_KEY, Constants.PROVIDER));
	}

	public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
		if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
			return protocol.refer(type, url);
		}
		return buildInvokerChain(protocol.refer(type, url),
				Constants.REFERENCE_FILTER_KEY, Constants.CONSUMER);
	}

	public void destroy() {
		protocol.destroy();
	}

	/**
	 * 构建invoker调用的filter链
	 * 
	 * @param invoker
	 * @param key
	 * @param group
	 * @return
	 */
	private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker,
			String key, String group) {
		Invoker<T> last = invoker;
		List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class)
				.getActivateExtension(invoker.getUrl(), key, group);
		if (filters.size() == 0) {
			return last;
		}
		for (int i = filters.size() - 1; i >= 0; i--) {
			final Filter filter = filters.get(i);
			final Invoker<T> next = last;
			last = new Invoker<T>() {

				public Class<T> getInterface() {
					return invoker.getInterface();
				}

				public URL getUrl() {
					return invoker.getUrl();
				}

				public boolean isAvailable() {
					return invoker.isAvailable();
				}

				public Result invoke(Invocation invocation) throws RpcException {
					return filter.invoke(next, invocation);
				}

				public void destroy() {
					invoker.destroy();
				}

				@Override
				public String toString() {
					return invoker.toString();
				}
			};
		}
		return last;
	}

}