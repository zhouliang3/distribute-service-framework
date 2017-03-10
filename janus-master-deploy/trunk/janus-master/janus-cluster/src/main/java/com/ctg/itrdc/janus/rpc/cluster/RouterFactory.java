package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;
import com.ctg.itrdc.janus.rpc.Invocation;

/**
 * 路由器工厂类
 * 
 * @see com.ctg.itrdc.janus.rpc.cluster.Cluster#join(Directory)
 * @see com.ctg.itrdc.janus.rpc.cluster.Directory#list(Invocation)
 * @author Administrator
 */
@SPI
public interface RouterFactory {

	/**
	 * 创建路由规则
	 * 
	 * @param url
	 * @return router
	 */
	@Adaptive("protocol")
	Router getRouter(URL url);

}