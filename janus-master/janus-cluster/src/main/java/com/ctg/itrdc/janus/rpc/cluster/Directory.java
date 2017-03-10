package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.Node;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;

import java.util.List;

/**
 * 目录服务
 * 
 * @see com.ctg.itrdc.janus.rpc.cluster.Cluster#join(Directory)
 * @author Administrator
 */
public interface Directory<T> extends Node {

	/**
	 * 获取调用接口
	 * 
	 * @return service type.
	 */
	Class<T> getInterface();

	/**
	 * 根据invocation对象，获取批量调用者
	 * 
	 * @return invokers
	 */
	List<Invoker<T>> list(Invocation invocation) throws RpcException;

}