package com.ctg.itrdc.janus.rpc.cluster.loadbalance;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.cluster.LoadBalance;

import java.util.List;

/**
 * LoadBalance抽象实现
 * 
 * @author Administrator
 */
public abstract class AbstractLoadBalance implements LoadBalance {

	/**
	 * 选择服务。<br/>
	 * 如果服务列表为空，返回空；如果只有一个，返回该服务。否则根据不同算法进行选取
	 */
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url,
			Invocation invocation) {
		if (invokers == null || invokers.size() == 0)
			return null;
		if (invokers.size() == 1)
			return invokers.get(0);
		return doSelect(invokers, url, invocation);
	}

	protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers,
			URL url, Invocation invocation);

	/**
	 * 获取权重值
	 * 
	 * @param invoker
	 * @param invocation
	 * @return
	 */
	protected int getWeight(Invoker<?> invoker, Invocation invocation) {
		int weight = invoker.getUrl().getMethodParameter(
				invocation.getMethodName(), Constants.WEIGHT_KEY,
				Constants.DEFAULT_WEIGHT);
		if (weight > 0) {
			long timestamp = invoker.getUrl().getParameter(
					Constants.TIMESTAMP_KEY, 0L);
			if (timestamp > 0L) {
				int uptime = (int) (System.currentTimeMillis() - timestamp);
				int warmup = invoker.getUrl().getParameter(
						Constants.WARMUP_KEY, Constants.DEFAULT_WARMUP);
				if (uptime > 0 && uptime < warmup) {
					weight = calculateWarmupWeight(uptime, warmup, weight);
				}
			}
		}
		return weight;
	}

	static int calculateWarmupWeight(int uptime, int warmup, int weight) {
		int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
		return ww < 1 ? 1 : (ww > weight ? weight : ww);
	}

}