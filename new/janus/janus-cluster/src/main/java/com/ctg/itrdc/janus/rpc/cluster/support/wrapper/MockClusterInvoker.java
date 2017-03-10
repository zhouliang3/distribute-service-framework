package com.ctg.itrdc.janus.rpc.cluster.support.wrapper;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.rpc.*;
import com.ctg.itrdc.janus.rpc.cluster.Directory;
import com.ctg.itrdc.janus.rpc.support.MockInvoker;

import java.util.List;

/**
 * Mock集群调用者
 * 
 * @author Administrator
 */
public class MockClusterInvoker<T> implements Invoker<T> {

	private static final Logger logger = LoggerFactory
			.getLogger(MockClusterInvoker.class);

	private final Directory<T> directory;

	private final Invoker<T> invoker;

	public MockClusterInvoker(Directory<T> directory, Invoker<T> invoker) {
		this.directory = directory;
		this.invoker = invoker;
	}

	/**
	 * 获取调用方url
	 */
	public URL getUrl() {
		return directory.getUrl();
	}

	/**
	 * 判断当前目录服务是否可用
	 */
	public boolean isAvailable() {
		return directory.isAvailable();
	}

	/**
	 * 销毁
	 */
	public void destroy() {
		this.invoker.destroy();
	}

	public Class<T> getInterface() {
		return directory.getInterface();
	}

	/**
	 * 执行调用,分如下的场景进行调用:<br/>
	 * 1.如果url参数中不存在mock，或者存在，但其值为false，不走mock调用<br/>
	 * 2.如果url参数中的mock参数以force开头，直接走mock调用流程<br/>
	 * 3.如果url参数中有mock参数，但是不以false或者force开头。先进行调用，如果有抛出异常RpcException，将走mock流程。
	 */
	public Result invoke(Invocation invocation) throws RpcException {
		Result result = null;

		String value = directory
				.getUrl()
				.getMethodParameter(invocation.getMethodName(),
						Constants.MOCK_KEY, Boolean.FALSE.toString()).trim();
		if (value.length() == 0 || value.equalsIgnoreCase("false")) {
			return this.invoker.invoke(invocation);
		}
		if (value.startsWith("force")) {
			if (logger.isWarnEnabled()) {
				logger.info("force-mock: " + invocation.getMethodName()
						+ " force-mock enabled , url : " + directory.getUrl());
			}
			// force:direct mock
			return doMockInvoke(invocation, null);
		}
		// fail-mock
		try {
			result = this.invoker.invoke(invocation);
		} catch (RpcException e) {
			if (e.isBiz()) {
				throw e;
			} else {
				if (logger.isWarnEnabled()) {
					logger.info(
							"fail-mock: " + invocation.getMethodName()
									+ " fail-mock enabled , url : "
									+ directory.getUrl(), e);
				}
				result = doMockInvoke(invocation, e);
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Result doMockInvoke(Invocation invocation, RpcException e) {
		Result result = null;
		Invoker<T> minvoker;

		List<Invoker<T>> mockInvokers = selectMockInvoker(invocation);
		if (mockInvokers == null || mockInvokers.size() == 0) {
			minvoker = (Invoker<T>) new MockInvoker(directory.getUrl());
		} else {
			minvoker = mockInvokers.get(0);
		}
		try {
			result = minvoker.invoke(invocation);
		} catch (RpcException me) {
			if (me.isBiz()) {
				result = new RpcResult(me.getCause());
			} else {
				throw new RpcException(me.getCode(), getMockExceptionMessage(e,
						me), me.getCause());
			}
			//
		} catch (Throwable me) {
			throw new RpcException(getMockExceptionMessage(e, me),
					me.getCause());
		}
		return result;
	}

	private String getMockExceptionMessage(Throwable t, Throwable mt) {
		String msg = "mock error : " + mt.getMessage();
		if (t != null) {
			msg = msg + ", invoke error is :" + StringUtils.toString(t);
		}
		return msg;
	}

	/**
	 * 返回MockInvoker 契约：
	 * directory根据invocation中是否有Constants.INVOCATION_NEED_MOCK，来判断获取的是一个normal
	 * invoker 还是一个 mock invoker 如果directorylist 返回多个mock invoker，只使用第一个invoker.
	 * 
	 * @param invocation
	 * @return
	 */
	private List<Invoker<T>> selectMockInvoker(Invocation invocation) {
		// TODO generic invoker？
		if (invocation instanceof RpcInvocation) {
			// 存在隐含契约(虽然在接口声明中增加描述，但扩展性会存在问题.同时放在attachement中的做法需要改进
			((RpcInvocation) invocation).setAttachment(
					Constants.INVOCATION_NEED_MOCK, Boolean.TRUE.toString());
			// directory根据invocation中attachment是否有Constants.INVOCATION_NEED_MOCK，来判断获取的是normal
			// invokers or mock invokers
			List<Invoker<T>> invokers = directory.list(invocation);
			return invokers;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "invoker :" + this.invoker + ",directory: " + this.directory;
	}
}