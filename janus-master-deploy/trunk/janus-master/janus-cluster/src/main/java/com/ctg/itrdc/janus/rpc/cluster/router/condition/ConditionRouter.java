/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ctg.itrdc.janus.rpc.cluster.router.condition;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.common.utils.UrlUtils;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.Router;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 条件路由。例如： registry.register(URL.valueOf(
 * "condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule="
 * + URL.encode("host = 10.20.153.10 => host = 10.20.153.11") + "));
 * 
 * 条件路由中的url可带参数，其中几个重要的参数如下：
 * 
 * <pre>
 * 1.force=false
 *   当路由结果为空时，是否强制执行，如果不强制执行，路由结果为空的路由规则将自动失效，可不填，缺省为flase。
 * 2.runtime=false
 *   是否在每次调用时执行路由规则，否则只在提供者地址列表变更时预先执行并缓存结果，调用时直接从缓存中获取路由结果。
 *   如果用了参数路由，必须设为true，需要注意设置会影响调用的性能，可不填，缺省为flase。
 * 3.priority=1
 *   路由规则的优先级，用于排序，优先级越大越靠前执行，可不填，缺省为0。
 * </pre>
 * 
 * 条件规则如下：
 * 
 * <pre>
 * 基于条件表达式的路由规则，如：
 * host = 10.20.153.10 => host = 10.20.153.11
 * 
 * 规则：
 *   1."=>"之前的为消费者匹配条件，所有参数和消费者的URL进行对比，当消费者满足匹配条件时，对该消费者执行后面的过滤规则。
 *   2."=>"之后为提供者地址列表的过滤条件，所有参数和提供者的URL进行对比，消费者最终只拿到过滤后的地址列表。
 *   3.如果匹配条件为空，表示对所有消费方应用，如：=> host != 10.20.153.11
 *   4.如果过滤条件为空，表示禁止访问，如：host = 10.20.153.10 =>
 * 表达式：
 * 
 *   1.参数支持：
 *     服务调用信息，如：method, argument 等 (暂不支持参数路由)
 *     URL本身的字段，如：protocol, host, port 等
 *     以及URL上的所有参数，如：application, organization 等
 *   2.条件支持：
 *     等号"="表示"匹配"，如：host = 10.20.153.10
 *     不等号"!="表示"不匹配"，如：host != 10.20.153.10
 *   3.值支持：
 *     以逗号","分隔多个值，如：host != 10.20.153.10,10.20.153.11
 *     以星号"*"结尾，表示通配，如：host != 10.20.*
 *     以美元符"$"开头，表示引用消费者参数，如：host = $host
 * 示例：
 * 
 * 1. 排除预发布机：
 * => host != 172.22.3.91
 * 
 * 2. 白名单：(注意：一个服务只能有一条白名单规则，否则两条规则交叉，就都被筛选掉了)
 * host != 10.20.153.10,10.20.153.11 =>
 * 
 * 3. 黑名单：
 * host = 10.20.153.10,10.20.153.11 =>
 * 
 * 4. 服务寄宿在应用上，只暴露一部分的机器，防止整个集群挂掉：
 * => host = 172.22.3.1*,172.22.3.2*
 * 
 * 5. 为重要应用提供额外的机器：
 * application != kylin => host != 172.22.3.95,172.22.3.96
 * 
 * 6. 读写分离：
 * method = find*,list*,get*,is* => host = 172.22.3.94,172.22.3.95,172.22.3.96
 * method != find*,list*,get*,is* => host = 172.22.3.97,172.22.3.98
 * 
 * 7. 前后台分离：
 * application = bops => host = 172.22.3.91,172.22.3.92,172.22.3.93
 * application != bops => host = 172.22.3.94,172.22.3.95,172.22.3.96
 * 
 * 8. 隔离不同机房网段：
 * host != 172.22.3.* => host != 172.22.3.*
 * 
 * 9. 提供者与消费者部署在同集群内，本机只访问本机的服务：
 * => host = $host
 * 
 * 10.直接返回false
 * host = 10.20.153.10 => false
 * 
 * 11.匹配条件为true
 * true => host = 10.20.153.10
 * 
 * </pre>
 * 
 *
 */
public class ConditionRouter implements Router, Comparable<Router> {

	private static final Logger logger = LoggerFactory
			.getLogger(ConditionRouter.class);

	/**
	 * url参数
	 */
	private final URL url;

	/**
	 * 优先级
	 */
	private final int priority;

	/**
	 * 当路由结果为空时，是否强制执行，如果不强制执行，路由结果为空的路由规则将自动失效，可不填，缺省为flase。
	 */
	private final boolean force;

	/**
	 * 左边的when条件表达式
	 */
	private final Map<String, MatchPair> whenCondition;

	/**
	 * 右边的结果执行表达式
	 */
	private final Map<String, MatchPair> thenCondition;

	/**
	 * 根据url参数，构造条件表达式
	 * 
	 * 如果rule左边表达式为true，表示消费方匹配全部条件；如果rule右边表达式为false，表示服务方全部匹配失败
	 * 
	 * @param url
	 */
	public ConditionRouter(URL url) {
		this.url = url;
		this.priority = url.getParameter(Constants.PRIORITY_KEY, 0);
		this.force = url.getParameter(Constants.FORCE_KEY, false);
		try {
			String rule = url.getParameterAndDecoded(Constants.RULE_KEY);
			if (rule == null || rule.trim().length() == 0) {
				throw new IllegalArgumentException("Illegal route rule!");
			}
			rule = rule.replace("consumer.", "").replace("provider.", "");
			int i = rule.indexOf("=>");
			String whenRule = i < 0 ? null : rule.substring(0, i).trim();
			String thenRule = i < 0 ? rule.trim() : rule.substring(i + 2)
					.trim();
			Map<String, MatchPair> when = StringUtils.isBlank(whenRule)
					|| "true".equals(whenRule) ? new HashMap<String, MatchPair>()
					: parseRule(whenRule);
			Map<String, MatchPair> then = StringUtils.isBlank(thenRule)
					|| "false".equals(thenRule) ? null : parseRule(thenRule);
			// NOTE: When条件是允许为空的，外部业务来保证类似的约束条件
			this.whenCondition = when;
			this.thenCondition = then;
		} catch (ParseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * 执行规则的路由
	 */
	public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url,
			Invocation invocation) throws RpcException {
		if (invokers == null || invokers.size() == 0) {
			return invokers;
		}
		try {
			if (!matchWhen(url)) {
				return invokers;
			}
			List<Invoker<T>> result = new ArrayList<Invoker<T>>();
			if (thenCondition == null) {
				logger.warn("The current consumer in the service blacklist. consumer: "
						+ NetUtils.getLocalHost()
						+ ", service: "
						+ url.getServiceKey());
				return result;
			}
			for (Invoker<T> invoker : invokers) {
				if (matchThen(invoker.getUrl(), url)) {
					result.add(invoker);
				}
			}
			if (result.size() > 0) {
				return result;
			} else if (force) {
				logger.warn("The route result is empty and force execute. consumer: "
						+ NetUtils.getLocalHost()
						+ ", service: "
						+ url.getServiceKey()
						+ ", router: "
						+ url.getParameterAndDecoded(Constants.RULE_KEY));
				return result;
			}
		} catch (Throwable t) {
			logger.error("Failed to execute condition router rule: " + getUrl()
					+ ", invokers: " + invokers + ", cause: " + t.getMessage(),
					t);
		}
		return invokers;
	}

	public URL getUrl() {
		return url;
	}

	/**
	 * 比较两条匹配规则的优先级
	 */
	public int compareTo(Router o) {
		if (o == null || o.getClass() != ConditionRouter.class) {
			return 1;
		}
		ConditionRouter c = (ConditionRouter) o;
		return this.priority == c.priority ? url.toFullString().compareTo(
				c.url.toFullString()) : (this.priority > c.priority ? 1 : -1);
	}

	public boolean matchWhen(URL url) {
		return matchCondition(whenCondition, url, null);
	}

	public boolean matchThen(URL url, URL param) {
		return thenCondition != null
				&& matchCondition(thenCondition, url, param);
	}

	private boolean matchCondition(Map<String, MatchPair> condition, URL url,
			URL param) {
		Map<String, String> sample = url.toMap();
		for (Map.Entry<String, String> entry : sample.entrySet()) {
			String key = entry.getKey();
			MatchPair pair = condition.get(key);
			if (pair != null && !pair.isMatch(entry.getValue(), param)) {
				return false;
			}
		}
		return true;
	}

	private static Pattern ROUTE_PATTERN = Pattern
			.compile("([&!=,]*)\\s*([^&!=,\\s]+)");

	/**
	 * 解析规则
	 * 
	 * @param rule
	 * @return
	 * @throws java.text.ParseException
	 */
	private static Map<String, MatchPair> parseRule(String rule)
			throws ParseException {
		Map<String, MatchPair> condition = new HashMap<String, MatchPair>();
		if (StringUtils.isBlank(rule)) {
			return condition;
		}
		// 匹配或不匹配Key-Value对
		MatchPair pair = null;
		// 多个Value值
		Set<String> values = null;
		final Matcher matcher = ROUTE_PATTERN.matcher(rule);
		while (matcher.find()) { // 逐个匹配
			String separator = matcher.group(1);
			String content = matcher.group(2);
			// 表达式开始
			if (separator == null || separator.length() == 0) {
				pair = new MatchPair();
				condition.put(content, pair);
			}
			// KV开始
			else if ("&".equals(separator)) {
				if (condition.get(content) == null) {
					pair = new MatchPair();
					condition.put(content, pair);
				} else {
					condition.put(content, pair);
				}
			}
			// KV的Value部分开始
			else if ("=".equals(separator)) {
				if (pair == null)
					throw new ParseException("Illegal route rule \"" + rule
							+ "\", The error char '" + separator
							+ "' at index " + matcher.start() + " before \""
							+ content + "\".", matcher.start());

				values = pair.matches;
				values.add(content);
			}
			// KV的Value部分开始
			else if ("!=".equals(separator)) {
				if (pair == null)
					throw new ParseException("Illegal route rule \"" + rule
							+ "\", The error char '" + separator
							+ "' at index " + matcher.start() + " before \""
							+ content + "\".", matcher.start());

				values = pair.mismatches;
				values.add(content);
			}
			// KV的Value部分的多个条目
			else if (",".equals(separator)) { // 如果为逗号表示
				if (values == null || values.size() == 0)
					throw new ParseException("Illegal route rule \"" + rule
							+ "\", The error char '" + separator
							+ "' at index " + matcher.start() + " before \""
							+ content + "\".", matcher.start());
				values.add(content);
			} else {
				throw new ParseException("Illegal route rule \"" + rule
						+ "\", The error char '" + separator + "' at index "
						+ matcher.start() + " before \"" + content + "\".",
						matcher.start());
			}
		}
		return condition;
	}

	/**
	 * 规则匹配模型。包含匹配和不匹配
	 * 
	 * @author Administrator
	 */
	private static final class MatchPair {

		/**
		 * 匹配列表
		 */
		final Set<String> matches = new HashSet<String>();

		/**
		 * 不匹配列表
		 */
		final Set<String> mismatches = new HashSet<String>();

		/**
		 * 判断是否匹配.
		 * 
		 * 对于匹配的规则。只要有一个匹配才返回true <br/>
		 * 对于非匹配的规则。 只要有一个不匹配，立即返回false
		 * 
		 * @param value
		 * @param param
		 * @return
		 */
		public boolean isMatch(String value, URL param) {
			if (matches.size() > 0) { // fix by Administrator
				for (String match : matches) {
					if (UrlUtils.isMatchGlobPattern(match, value, param)) {
						return true;
					}
				}
				return false;
			}
			for (String mismatch : mismatches) {
				if (UrlUtils.isMatchGlobPattern(mismatch, value, param)) {
					return false;
				}
			}
			return true;
		}
	}
}