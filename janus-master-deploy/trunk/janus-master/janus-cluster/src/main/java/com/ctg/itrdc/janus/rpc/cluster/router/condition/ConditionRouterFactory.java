package com.ctg.itrdc.janus.rpc.cluster.router.condition;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.cluster.Router;
import com.ctg.itrdc.janus.rpc.cluster.RouterFactory;

/**
 * ConditionRouterFactory
 * 
 * @author Administrator
 */
public class ConditionRouterFactory implements RouterFactory {

	public static final String NAME = "condition";

	public Router getRouter(URL url) {
		return new ConditionRouter(url);
	}

}