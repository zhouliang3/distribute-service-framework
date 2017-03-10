package com.ctg.itrdc.janus.rpc.cluster.loadbalance;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 * 
 * @author Administrator
 */
public class RandomLoadBalance extends AbstractLoadBalance {

	public static final String NAME = "random";

	private final Random random = new Random();

	protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url,
			Invocation invocation) {
		int length = invokers.size(); // 总个数
		int totalWeight = 0; // 总权重
		boolean sameWeight = true; // 权重是否都一样
		for (int i = 0; i < length; i++) {
			int weight = getWeight(invokers.get(i), invocation);
			totalWeight += weight; // 累计总权重
			if (sameWeight && i > 0
					&& weight != getWeight(invokers.get(i - 1), invocation)) {
				sameWeight = false; // 计算所有权重是否一样
			}
		}
		if (totalWeight > 0 && !sameWeight) {
			// 如果权重不相同且权重大于0则按总权重数随机
			int offset = random.nextInt(totalWeight);
			// 并确定随机值落在哪个片断上
			for (int i = 0; i < length; i++) {
				offset -= getWeight(invokers.get(i), invocation);
				if (offset < 0) {
					return invokers.get(i);
				}
			}
		}
		// 如果权重相同或权重为0则均等随机
		return invokers.get(random.nextInt(length));
	}

}