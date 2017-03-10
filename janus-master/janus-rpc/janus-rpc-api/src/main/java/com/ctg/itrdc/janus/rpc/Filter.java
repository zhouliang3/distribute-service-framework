package com.ctg.itrdc.janus.rpc;

import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 调用过滤链. (SPI, Singleton, ThreadSafe)
 * 
 * @author Administrator
 */
@SPI
public interface Filter {

	/**
	 * invoke调用链
	 * 
	 * <code>
	 * // before filter
	 * Result result = invoker.invoke(invocation);
	 * // after filter
	 * return result;
	 * </code>
	 * 
	 * @see com.ctg.itrdc.janus.rpc.Invoker#invoke(Invocation)
	 * @param invoker
	 *            service
	 * @param invocation
	 *            invocation.
	 * @return invoke result.
	 * @throws RpcException
	 */
	Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException;

}