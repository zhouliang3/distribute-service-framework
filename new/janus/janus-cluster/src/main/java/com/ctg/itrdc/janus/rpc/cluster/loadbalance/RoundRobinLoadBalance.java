package com.ctg.itrdc.janus.rpc.cluster.loadbalance;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.AtomicPositiveInteger;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 轮询负载均衡算法
 *
 * @author Administrator
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

	public static final String NAME = "roundrobin";

	private final ConcurrentMap<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();

	private final ConcurrentMap<String, AtomicPositiveInteger> weightSequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();

	protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url,
			Invocation invocation) {
		String key = invokers.get(0).getUrl().getServiceKey() + "."
				+ invocation.getMethodName();
		int length = invokers.size(); // 总个数
		int maxWeight = 0; // 最大权重
		int minWeight = Integer.MAX_VALUE; // 最小权重
		for (int i = 0; i < length; i++) {
			int weight = getWeight(invokers.get(i), invocation);
			maxWeight = Math.max(maxWeight, weight); // 累计最大权重
			minWeight = Math.min(minWeight, weight); // 累计最小权重
		}
		if (maxWeight > 0 && minWeight < maxWeight) { // 权重不一样
			AtomicPositiveInteger weightSequence = weightSequences.get(key);
			if (weightSequence == null) {
				weightSequences.putIfAbsent(key, new AtomicPositiveInteger());
				weightSequence = weightSequences.get(key);
			}
			int currentWeight = weightSequence.getAndIncrement() % maxWeight;
			List<Invoker<T>> weightInvokers = new ArrayList<Invoker<T>>();
			for (Invoker<T> invoker : invokers) { // 筛选权重大于当前权重基数的Invoker
				if (getWeight(invoker, invocation) > currentWeight) {
					weightInvokers.add(invoker);
				}
			}
			int weightLength = weightInvokers.size();
			if (weightLength == 1) {
				return weightInvokers.get(0);
			} else if (weightLength > 1) {
				invokers = weightInvokers;
				length = invokers.size();
			}
		}
		AtomicPositiveInteger sequence = sequences.get(key);
		if (sequence == null) {
			sequences.putIfAbsent(key, new AtomicPositiveInteger());
			sequence = sequences.get(key);
		}
		// 取模轮循
		return invokers.get(sequence.getAndIncrement() % length);
	}

}