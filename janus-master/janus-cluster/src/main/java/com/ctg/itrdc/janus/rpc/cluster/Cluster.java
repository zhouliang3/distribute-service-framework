package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.support.FailoverCluster;

/**
 * Cluster. (SPI, Singleton, ThreadSafe)
 * 
 * <a href="http://en.wikipedia.org/wiki/Computer_cluster">Cluster</a> <a
 * href="http://en.wikipedia.org/wiki/Fault-tolerant_system">Fault-Tolerant</a>
 * 
 * @author Administrator
 */
@SPI(FailoverCluster.NAME)
public interface Cluster {

	/**
	 * 将目录里面的所有的invokers进行合并 to a virtual invoker.
	 * 
	 * @param <T>
	 * @param directory
	 * @return cluster invoker
	 * @throws RpcException
	 */
	@Adaptive
	<T> Invoker<T> join(Directory<T> directory) throws RpcException;

}