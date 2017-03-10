package com.ctg.itrdc.janus.rpc.filter;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.RpcStatus;

/**
 * 调用访问限制过滤器
 * 
 * 根据当前服务的活跃消费方，进行流控
 * 
 * @author Administrator
 */
@Activate(group = Constants.CONSUMER, value = Constants.ACTIVES_KEY)
public class ActiveLimitFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		URL url = invoker.getUrl();
		String methodName = invocation.getMethodName();
		int max = invoker.getUrl().getMethodParameter(methodName,
				Constants.ACTIVES_KEY, 0);
		RpcStatus count = RpcStatus.getStatus(invoker.getUrl(),
				invocation.getMethodName());
		if (max > 0) {
			activeControl(invoker, invocation, max, count);
		}
		try {
			long begin = System.currentTimeMillis();
			RpcStatus.beginCount(url, methodName);
			try {
				Result result = invoker.invoke(invocation);
				RpcStatus.endCount(url, methodName, System.currentTimeMillis()
						- begin, true);
				return result;
			} catch (RuntimeException t) {
				RpcStatus.endCount(url, methodName, System.currentTimeMillis()
						- begin, false);
				throw t;
			}
		} finally {
			if (max > 0) {
				synchronized (count) {
					count.notify();
				}
			}
		}
	}

	/**
	 * 流控处理。如果当前活跃的消费调用总数少于max限制，直接返回不进行控制。否则一致等待到当前的消费调用总数少于max的限制为止。
	 * 
	 * 如果在设置的超时时间内，未满足，抛出超时异常。
	 * 
	 * @param invoker
	 * @param invocation
	 * @param max
	 * @param count
	 */
	private void activeControl(Invoker<?> invoker, Invocation invocation,
			int max, RpcStatus count) {
		long timeout = invoker.getUrl().getMethodParameter(
				invocation.getMethodName(), Constants.TIMEOUT_KEY, 0);
		long start = System.currentTimeMillis();
		long remain = timeout;
		int active = count.getActive();
		if (active < max) {
			return;
		}
		synchronized (count) {
			while ((active = count.getActive()) >= max) {
				try {
					count.wait(remain);
				} catch (InterruptedException e) {
				}
				long elapsed = System.currentTimeMillis() - start;
				remain = timeout - elapsed;
				if (remain <= 0) {
					throw new RpcException(
							"Waiting concurrent invoke timeout in client-side for service:  "
									+ invoker.getInterface().getName()
									+ ", method: " + invocation.getMethodName()
									+ ", elapsed: " + elapsed + ", timeout: "
									+ timeout + ". concurrent invokes: "
									+ active
									+ ". max concurrent invoke limit: " + max);
				}
			}
		}
	}
}