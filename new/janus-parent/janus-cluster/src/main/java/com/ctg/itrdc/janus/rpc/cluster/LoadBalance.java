package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.loadbalance.RandomLoadBalance;

import java.util.List;

/**
 * 负载均衡器
 * 
 * @see com.ctg.itrdc.janus.rpc.cluster.Cluster#join(Directory)
 * @author Administrator
 */
@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {

	/**
	 * 从服务列表中选择一个服务
	 * 
	 * @param invokers
	 *            invokers.
	 * @param url
	 *            refer url
	 * @param invocation
	 *            invocation.
	 * @return selected invoker.
	 */
	@Adaptive("loadbalance")
	<T> Invoker<T> select(List<Invoker<T>> invokers, URL url,
						  Invocation invocation) throws RpcException;

}