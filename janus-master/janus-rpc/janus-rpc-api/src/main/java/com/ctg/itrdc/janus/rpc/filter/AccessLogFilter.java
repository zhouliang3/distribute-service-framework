package com.ctg.itrdc.janus.rpc.filter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.json.JSON;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.ConcurrentHashSet;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.NamedThreadFactory;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcContext;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * 记录Service的Access Log。
 * <p>
 * 使用的Logger key是<code><b>janus.accesslog</b></code>。 如果想要配置Access
 * Log只出现在指定的Appender中，可以在Log4j中注意配置上additivity。配置示例: <code>
 * <pre>
 * &lt;logger name="<b>janus.accesslog</b>" <font color="red">additivity="false"</font>&gt;
 *    &lt;level value="info" /&gt;
 *    &lt;appender-ref ref="foo" /&gt;
 * &lt;/logger&gt;
 * </pre></code>
 * 
 * @author Administrator
 */
@Activate(group = Constants.PROVIDER, value = Constants.ACCESS_LOG_KEY)
public class AccessLogFilter implements Filter {

	private static final Logger logger = LoggerFactory
			.getLogger(AccessLogFilter.class);

	private static final String ACCESS_LOG_KEY = "janus.accesslog";

	private static final String FILE_DATE_FORMAT = "yyyyMMdd";

	private static final String MESSAGE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final int LOG_MAX_BUFFER = 5000;

	private static final long LOG_OUTPUT_INTERVAL = 5000;

	private final ConcurrentMap<String, Set<String>> logQueue = new ConcurrentHashMap<String, Set<String>>();

	private final ScheduledExecutorService logScheduled = Executors
			.newScheduledThreadPool(2, new NamedThreadFactory(
					"janus-Access-Log", true));

	private volatile ScheduledFuture<?> logFuture = null;

	private class LogTask implements Runnable {
		public void run() {
			try {
				if (logQueue != null && logQueue.size() > 0) {
					for (Map.Entry<String, Set<String>> entry : logQueue
							.entrySet()) {
						try {
							String accesslog = entry.getKey();
							Set<String> logSet = entry.getValue();
							File file = new File(accesslog);
							File dir = file.getParentFile();
							if (null != dir && !dir.exists()) {
								dir.mkdirs();
							}
							if (logger.isDebugEnabled()) {
								logger.debug("Append log to " + accesslog);
							}
							if (file.exists()) {
								String now = new SimpleDateFormat(
										FILE_DATE_FORMAT).format(new Date());
								String last = new SimpleDateFormat(
										FILE_DATE_FORMAT).format(new Date(file
										.lastModified()));
								if (!now.equals(last)) {
									File archive = new File(
											file.getAbsolutePath() + "." + last);
									file.renameTo(archive);
								}
							}
							FileWriter writer = new FileWriter(file, true);
							try {
								for (Iterator<String> iterator = logSet
										.iterator(); iterator.hasNext(); iterator
										.remove()) {
									writer.write(iterator.next());
									writer.write("\r\n");
								}
								writer.flush();
							} finally {
								writer.close();
							}
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void init() {
		if (logFuture == null) {
			synchronized (logScheduled) {
				if (logFuture == null) {
					logFuture = logScheduled.scheduleWithFixedDelay(
							new LogTask(), LOG_OUTPUT_INTERVAL,
							LOG_OUTPUT_INTERVAL, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	private void log(String accesslog, String logmessage) {
		init();
		Set<String> logSet = logQueue.get(accesslog);
		if (logSet == null) {
			logQueue.putIfAbsent(accesslog, new ConcurrentHashSet<String>());
			logSet = logQueue.get(accesslog);
		}
		if (logSet.size() < LOG_MAX_BUFFER) {
			logSet.add(logmessage);
		}
	}

	public Result invoke(Invoker<?> invoker, Invocation inv)
			throws RpcException {
		try {
			URL url = invoker.getUrl();
			String accesslog = url.getParameter(
					Constants.ACCESS_LOG_KEY);
			if (ConfigUtils.isNotEmpty(accesslog)) {
				RpcContext context = RpcContext.getContext();
				String serviceName = invoker.getInterface().getName();
				String version = url.getParameter(
						Constants.VERSION_KEY);
				String group = url.getParameter(
						Constants.GROUP_KEY);
				StringBuilder sn = new StringBuilder();
				sn.append("[")
						.append(new SimpleDateFormat(MESSAGE_DATE_FORMAT)
								.format(new Date())).append("] ")
						.append(context.getRemoteHost()).append(":")
						.append(context.getRemotePort()).append(" -> ")
						.append(context.getLocalHost()).append(":")
						.append(context.getLocalPort()).append(" - ");
				if (null != group && group.length() > 0) {
					sn.append(group).append("/");
				}
				sn.append(serviceName);
				if (null != version && version.length() > 0) {
					sn.append(":").append(version);
				}
				sn.append(" ");
				sn.append(inv.getMethodName());
				sn.append("(");
				Class<?>[] types = inv.getParameterTypes();
				if (types != null && types.length > 0) {
					boolean first = true;
					for (Class<?> type : types) {
						if (first) {
							first = false;
						} else {
							sn.append(",");
						}
						sn.append(type.getName());
					}
				}
				sn.append(") ");
				Object[] args = inv.getArguments();
				if (args != null && args.length > 0) {
					sn.append(JSON.json(args));
				}
				String msg = sn.toString();
				if (ConfigUtils.isDefault(accesslog)) {
					LoggerFactory.getLogger(
							ACCESS_LOG_KEY + "."
									+ invoker.getInterface().getName()).info(
							msg);
				} else {
					log(accesslog, msg);
				}
			}
		} catch (Throwable t) {
			logger.warn("Exception in AcessLogFilter of service(" + invoker
					+ " -> " + inv + ")", t);
		}
		return invoker.invoke(inv);
	}

}