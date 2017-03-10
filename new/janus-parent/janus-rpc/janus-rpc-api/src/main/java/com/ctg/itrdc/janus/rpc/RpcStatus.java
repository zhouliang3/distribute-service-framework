package com.ctg.itrdc.janus.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.ctg.itrdc.janus.common.URL;

/**
 * URL服务的调用统计
 * 
 * 记录当前服务对应的各类调用执行信息。存放在内存当中。
 * 
 * @see com.ctg.itrdc.janus.rpc.filter.ActiveLimitFilter
 * @see com.ctg.itrdc.janus.rpc.filter.ExecuteLimitFilter
 * @see com.ctg.itrdc.janus.rpc.cluster.loadbalance.LeastActiveLoadBalance
 * 
 * @author Administrator
 */
public class RpcStatus {

	/**
	 * 服务的统计信息。<br/>
	 * 其中key为服务的url，value为当前的RpcStatus统计信息
	 */
	private static final ConcurrentMap<String, RpcStatus> SERVICE_STATISTICS = new ConcurrentHashMap<String, RpcStatus>();

	/**
	 * 服务方法的调用统计信息<br/>
	 * 其中分了两级的map对象。其中一级key为应用服务的url。 value为方法级别的统计信息。<br/>
	 * 二级map的key为方法名称，value为对应的RpcStatus统计信息
	 */
	private static final ConcurrentMap<String, ConcurrentMap<String, RpcStatus>> METHOD_STATISTICS = new ConcurrentHashMap<String, ConcurrentMap<String, RpcStatus>>();

	/**
	 * 根据url，获取该url服务对应的Rpc统计信息
	 * 
	 * @param url
	 * @return status
	 */
	public static RpcStatus getStatus(URL url) {
		String uri = url.toIdentityString();
		RpcStatus status = SERVICE_STATISTICS.get(uri);
		if (status == null) {
			SERVICE_STATISTICS.putIfAbsent(uri, new RpcStatus());
			status = SERVICE_STATISTICS.get(uri);
		}
		return status;
	}

	/**
	 * 清除url的服务对应的调用统计信息
	 * 
	 * @param url
	 */
	public static void removeStatus(URL url) {
		String uri = url.toIdentityString();
		SERVICE_STATISTICS.remove(uri);
	}

	/**
	 * 获取url以及对应的调用方法，获取该url服务中的该方法对应的Rpc统计信息
	 * 
	 * @param url
	 * @param methodName
	 * @return status
	 */
	public static RpcStatus getStatus(URL url, String methodName) {
		String uri = url.toIdentityString();
		ConcurrentMap<String, RpcStatus> map = METHOD_STATISTICS.get(uri);
		if (map == null) {
			METHOD_STATISTICS.putIfAbsent(uri,
					new ConcurrentHashMap<String, RpcStatus>());
			map = METHOD_STATISTICS.get(uri);
		}
		RpcStatus status = map.get(methodName);
		if (status == null) {
			map.putIfAbsent(methodName, new RpcStatus());
			status = map.get(methodName);
		}
		return status;
	}

	/**
	 * 删除url服务中的方法对应的Rpc统计信息
	 * 
	 * @param url
	 */
	public static void removeStatus(URL url, String methodName) {
		String uri = url.toIdentityString();
		ConcurrentMap<String, RpcStatus> map = METHOD_STATISTICS.get(uri);
		if (map != null) {
			map.remove(methodName);
		}
	}

	/**
	 * 开始调用。需要增加活动的统计信息
	 * 
	 * @param url
	 */
	public static void beginCount(URL url, String methodName) {
		beginCount(getStatus(url));
		beginCount(getStatus(url, methodName));
	}

	/**
	 * 增加调用的活动数
	 * 
	 * @param status
	 */
	private static void beginCount(RpcStatus status) {
		status.active.incrementAndGet();
	}

	/**
	 * 结束调用
	 * 
	 * @param url
	 * @param elapsed
	 * @param succeeded
	 */
	public static void endCount(URL url, String methodName, long elapsed,
			boolean succeeded) {
		endCount(getStatus(url), elapsed, succeeded);
		endCount(getStatus(url, methodName), elapsed, succeeded);
	}

	/**
	 * 结束调用。并统计相关信息。<br/>
	 * 1.活动数减1 <br/>
	 * 2.总共服务数加1 <br/>
	 * 3.总共服务时长增加本次调用的时长 <br/>
	 * 4.最大服务时长 <br/>
	 * 5.记录成功的最大时长 <br/>
	 * 6.记录失败数和失败的最大时长 <br/>
	 * 
	 * @param status
	 * @param elapsed
	 * @param succeeded
	 */
	private static void endCount(RpcStatus status, long elapsed,
			boolean succeeded) {
		status.active.decrementAndGet();
		status.total.incrementAndGet();
		status.totalElapsed.addAndGet(elapsed);
		if (status.maxElapsed.get() < elapsed) {
			status.maxElapsed.set(elapsed);
		}
		if (succeeded) {
			if (status.succeededMaxElapsed.get() < elapsed) {
				status.succeededMaxElapsed.set(elapsed);
			}
		} else {
			status.failed.incrementAndGet();
			status.failedElapsed.addAndGet(elapsed);
			if (status.failedMaxElapsed.get() < elapsed) {
				status.failedMaxElapsed.set(elapsed);
			}
		}
	}

	private final ConcurrentMap<String, Object> values = new ConcurrentHashMap<String, Object>();

	/**
	 * 当前活动的数目，也即正在调用进行中的线程数
	 */
	private final AtomicInteger active = new AtomicInteger();

	/**
	 * 总共服务数
	 */
	private final AtomicLong total = new AtomicLong();

	/**
	 * 失败数
	 */
	private final AtomicInteger failed = new AtomicInteger();

	/**
	 * 总共服务时长
	 */
	private final AtomicLong totalElapsed = new AtomicLong();

	/**
	 * 失败服务时长
	 */
	private final AtomicLong failedElapsed = new AtomicLong();

	/**
	 * 最大服务时长
	 */
	private final AtomicLong maxElapsed = new AtomicLong();

	/**
	 * 失败的最长时长
	 */
	private final AtomicLong failedMaxElapsed = new AtomicLong();

	/**
	 * 成功调用的最长时长
	 */
	private final AtomicLong succeededMaxElapsed = new AtomicLong();

	private RpcStatus() {
	}

	/**
	 * set value.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		values.put(key, value);
	}

	/**
	 * get value.
	 * 
	 * @param key
	 * @return value
	 */
	public Object get(String key) {
		return values.get(key);
	}

	/**
	 * 获取当前活动数
	 * 
	 * @return active
	 */
	public int getActive() {
		return active.get();
	}

	/**
	 * 获取总共服务次数
	 * 
	 * @return total
	 */
	public long getTotal() {
		return total.longValue();
	}

	/**
	 * 获取总共的服务时长
	 * 
	 * @return total elapsed
	 */
	public long getTotalElapsed() {
		return totalElapsed.get();
	}

	/**
	 * 获取平均服务时长elapsed.
	 * 
	 * @return average elapsed
	 */
	public long getAverageElapsed() {
		long total = getTotal();
		if (total == 0) {
			return 0;
		}
		return getTotalElapsed() / total;
	}

	/**
	 * 获取消耗最久的服务时长
	 * 
	 * @return max elapsed
	 */
	public long getMaxElapsed() {
		return maxElapsed.get();
	}

	/**
	 * 获取失败的调用数
	 * 
	 * @return failed
	 */
	public int getFailed() {
		return failed.get();
	}

	/**
	 * 获取失败时长
	 * 
	 * @return failed elapsed
	 */
	public long getFailedElapsed() {
		return failedElapsed.get();
	}

	/**
	 * 获取失败的平均服务时长
	 * 
	 * @return failed average elapsed
	 */
	public long getFailedAverageElapsed() {
		long failed = getFailed();
		if (failed == 0) {
			return 0;
		}
		return getFailedElapsed() / failed;
	}

	/**
	 * 获取失败的最大调用服务时长
	 * 
	 * @return failed max elapsed
	 */
	public long getFailedMaxElapsed() {
		return failedMaxElapsed.get();
	}

	/**
	 * 获取成功的调用数
	 * 
	 * @return succeeded
	 */
	public long getSucceeded() {
		return getTotal() - getFailed();
	}

	/**
	 * 获取成功的服务时长
	 * 
	 * @return succeeded elapsed
	 */
	public long getSucceededElapsed() {
		return getTotalElapsed() - getFailedElapsed();
	}

	/**
	 * 获取成功的平均时长
	 * 
	 * @return succeeded average elapsed
	 */
	public long getSucceededAverageElapsed() {
		long succeeded = getSucceeded();
		if (succeeded == 0) {
			return 0;
		}
		return getSucceededElapsed() / succeeded;
	}

	/**
	 * 获取成功的最大时长
	 * 
	 * @return succeeded max elapsed.
	 */
	public long getSucceededMaxElapsed() {
		return succeededMaxElapsed.get();
	}

	/**
	 * 获取TPS
	 *
	 * @return tps
	 */
	public long getAverageTps() {
		if (getTotalElapsed() >= 1000L) {
			return getTotal() / (getTotalElapsed() / 1000L);
		}
		return getTotal();
	}

}