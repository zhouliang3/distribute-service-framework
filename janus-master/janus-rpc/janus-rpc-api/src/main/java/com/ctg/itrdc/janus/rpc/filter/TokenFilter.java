package com.ctg.itrdc.janus.rpc.filter;

import java.util.Map;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcContext;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * Token调用过滤器
 * 
 * 处理服务端的token和消费方传递过来的token比较
 * 
 * @author Administrator
 */
@Activate(group = Constants.PROVIDER, value = Constants.TOKEN_KEY)
public class TokenFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation inv)
			throws RpcException {
		String token = invoker.getUrl().getParameter(Constants.TOKEN_KEY);
		if (ConfigUtils.isNotEmpty(token)) {
			Class<?> serviceType = invoker.getInterface();
			Map<String, String> attachments = inv.getAttachments();
			String remoteToken = attachments == null ? null : attachments
					.get(Constants.TOKEN_KEY);
			if (!token.equals(remoteToken)) {
				throw new RpcException(
						"Invalid token! Forbid invoke remote service "
								+ serviceType + " method "
								+ inv.getMethodName() + "() from consumer "
								+ RpcContext.getContext().getRemoteHost()
								+ " to provider "
								+ RpcContext.getContext().getLocalHost());
			}
		}
		return invoker.invoke(inv);
	}

}