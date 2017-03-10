package com.ctg.itrdc.janus.rpc;

import com.ctg.itrdc.janus.common.Node;

/**
 * 动态调用. (API/SPI, Prototype, ThreadSafe)
 * 
 * @see com.ctg.itrdc.janus.rpc.Protocol#refer(Class,
 *      com.ctg.itrdc.janus.common.URL)
 * @see com.ctg.itrdc.janus.rpc.InvokerListener
 * @see com.ctg.itrdc.janus.rpc.protocol.AbstractInvoker
 * @author Administrator
 */
public interface Invoker<T> extends Node {

	/**
	 * 获取服务接口
	 * 
	 * @return service interface.
	 */
	Class<T> getInterface();

	/**
	 * 动态调用，得到调用结果
	 * 
	 * @param invocation
	 * @return result
	 * @throws RpcException
	 */
	Result invoke(Invocation invocation) throws RpcException;

}