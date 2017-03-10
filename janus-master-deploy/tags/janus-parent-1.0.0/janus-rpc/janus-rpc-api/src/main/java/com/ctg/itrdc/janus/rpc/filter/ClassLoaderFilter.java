package com.ctg.itrdc.janus.rpc.filter;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * ClassLoaderInvokerFilter
 * 
 * @author Administrator
 */
@Activate(group = Constants.PROVIDER, order = -30000)
public class ClassLoaderFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		ClassLoader ocl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
				invoker.getInterface().getClassLoader());
		try {
			return invoker.invoke(invocation);
		} finally {
			Thread.currentThread().setContextClassLoader(ocl);
		}
	}

}