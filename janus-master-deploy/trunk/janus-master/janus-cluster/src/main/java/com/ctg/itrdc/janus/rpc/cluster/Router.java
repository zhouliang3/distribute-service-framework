package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;

import java.util.List;

/**
 * Router接口
 * 
 * <a href="http://en.wikipedia.org/wiki/Routing">Routing</a>
 * 
 * @see com.ctg.itrdc.janus.rpc.cluster.Cluster#join(Directory)
 * @see com.ctg.itrdc.janus.rpc.cluster.Directory#list(Invocation)
 * @author Administrator
 */
public interface Router extends Comparable<Router> {

	/**
	 * 取得路由url
	 * 
	 * @return url
	 */
	URL getUrl();

	/**
	 * 进行规则路由
	 * 
	 * @param invokers
	 * @param url
	 *            refer url
	 * @param invocation
	 * @return routed invokers
	 * @throws RpcException
	 */
	<T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url,
							   Invocation invocation) throws RpcException;

}