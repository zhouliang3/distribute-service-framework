package com.ctg.itrdc.janus.rpc.cluster.support.wrapper;

import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.Cluster;
import com.ctg.itrdc.janus.rpc.cluster.Directory;

/**
 * cluster集群包装类
 * 
 * 通过ExtensionLoader中的wrapperCacheClasses获得
 * 
 * @author Administrator
 * 
 */
public class MockClusterWrapper implements Cluster {

	/**
	 * 原有集群引用对象
	 */
	private Cluster cluster;

	public MockClusterWrapper(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * mock对象聚合
	 */
	public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
		return new MockClusterInvoker<T>(directory,
				this.cluster.join(directory));
	}

}
