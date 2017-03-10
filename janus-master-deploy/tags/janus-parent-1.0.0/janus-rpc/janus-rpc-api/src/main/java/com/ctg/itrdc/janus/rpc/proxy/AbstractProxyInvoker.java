package com.ctg.itrdc.janus.rpc.proxy;

import java.lang.reflect.InvocationTargetException;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.RpcResult;

/**
 * InvokerWrapper
 * 
 * @author Administrator
 */
public abstract class AbstractProxyInvoker<T> implements Invoker<T> {

	/**
	 * 本地服务提供方对象
	 */
	private final T proxy;

	/**
	 * 类型
	 */
	private final Class<T> type;

	private final URL url;

	public AbstractProxyInvoker(T proxy, Class<T> type, URL url) {
		if (proxy == null) {
			throw new IllegalArgumentException("proxy == null");
		}
		if (type == null) {
			throw new IllegalArgumentException("interface == null");
		}
		if (!type.isInstance(proxy)) {
			throw new IllegalArgumentException(proxy.getClass().getName()
					+ " not implement interface " + type);
		}
		this.proxy = proxy;
		this.type = type;
		this.url = url;
	}

	/**
	 * 获取接口
	 */
	public Class<T> getInterface() {
		return type;
	}

	public URL getUrl() {
		return url;
	}

	/**
	 * 判断是否可用
	 */
	public boolean isAvailable() {
		return true;
	}

	public void destroy() {
	}

	/**
	 * 执行调用
	 */
	public Result invoke(Invocation invocation) throws RpcException {
		try {
			return new RpcResult(doInvoke(proxy, invocation.getMethodName(),
					invocation.getParameterTypes(), invocation.getArguments()));
		} catch (InvocationTargetException e) {
			return new RpcResult(e.getTargetException());
		} catch (Throwable e) {
			throw new RpcException("Failed to invoke remote proxy method "
					+ invocation.getMethodName() + " to " + getUrl()
					+ ", cause: " + e.getMessage(), e);
		}
	}

	protected abstract Object doInvoke(T proxy, String methodName,
			Class<?>[] parameterTypes, Object[] arguments) throws Throwable;

	@Override
	public String toString() {
		return getInterface() + " -> " + getUrl() == null ? " " : getUrl()
				.toString();
	}

}