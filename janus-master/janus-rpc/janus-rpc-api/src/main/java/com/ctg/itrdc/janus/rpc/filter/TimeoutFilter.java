package com.ctg.itrdc.janus.rpc.filter;

import java.util.Arrays;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * 如果执行timeout，则log记录下，不干涉服务的运行
 * 
 * @author Administrator
 */
@Activate(group = Constants.PROVIDER)
public class TimeoutFilter implements Filter {

	private static final Logger logger = LoggerFactory
			.getLogger(TimeoutFilter.class);

	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		long start = System.currentTimeMillis();
		Result result = invoker.invoke(invocation);
		long elapsed = System.currentTimeMillis() - start;
		if (invoker.getUrl() != null
				&& elapsed > invoker.getUrl().getMethodParameter(
						invocation.getMethodName(), "timeout",
						Integer.MAX_VALUE)) {
			if (logger.isWarnEnabled()) {
				logger.warn("invoke time out. method: "
						+ invocation.getMethodName() + "arguments: "
						+ Arrays.toString(invocation.getArguments())
						+ " , url is " + invoker.getUrl() + ", invoke elapsed "
						+ elapsed + " ms.");
			}
		}
		return result;
	}

}