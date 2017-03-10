package com.ctg.itrdc.janus.rpc.cluster.configurator;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.cluster.Configurator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 抽象url参数配置实现
 * 
 * @author Administrator
 */
public abstract class AbstractConfigurator implements Configurator {

	/**
	 * 配置url
	 * 
	 * 如果配置url的host为0.0.0.0，表示为所有的消费者url进行配置
	 */
	private final URL configuratorUrl;

	public AbstractConfigurator(URL url) {
		if (url == null) {
			throw new IllegalArgumentException("configurator url == null");
		}
		this.configuratorUrl = url;
	}

	public URL getUrl() {
		return configuratorUrl;
	}

	/**
	 * 对输入的url，进行配置
	 */
	public URL configure(URL url) {
		if (configuratorUrl == null || configuratorUrl.getHost() == null
				|| url == null || url.getHost() == null) {
			return url;
		}

		if (!Constants.ANYHOST_VALUE.equals(configuratorUrl.getHost())
				&& !url.getHost().equals(configuratorUrl.getHost())) {
			return url;
		}

		if (applicationEnabled(url)) {
			if (configuratorUrl.getPort() == 0
					|| url.getPort() == configuratorUrl.getPort()) {
				Set<String> condtionKeys = new HashSet<String>();
				condtionKeys.add(Constants.CATEGORY_KEY);
				condtionKeys.add(Constants.CHECK_KEY);
				condtionKeys.add(Constants.DYNAMIC_KEY);
				condtionKeys.add(Constants.ENABLED_KEY);
				for (Map.Entry<String, String> entry : configuratorUrl
						.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.startsWith("~")
							|| Constants.APPLICATION_KEY.equals(key)
							|| Constants.SIDE_KEY.equals(key)) {
						condtionKeys.add(key);
						if (value != null
								&& !Constants.ANY_VALUE.equals(value)
								&& !value.equals(url.getParameter(key
										.startsWith("~") ? key.substring(1)
										: key))) {
							return url;
						}
					}
				}
				return doConfigure(url,
						configuratorUrl.removeParameters(condtionKeys));
			}
		}
		return url;
	}

	/**
	 * @param url
	 */
	private boolean applicationEnabled(URL url) {
		String configApplication = configuratorUrl.getParameter(
				Constants.APPLICATION_KEY, configuratorUrl.getUsername());
		String currentApplication = url.getParameter(Constants.APPLICATION_KEY,
				url.getUsername());
		return configApplication == null
				|| Constants.ANY_VALUE.equals(configApplication)
				|| configApplication.equals(currentApplication);
	}

	public int compareTo(Configurator o) {
		if (o == null) {
			return -1;
		}
		return getUrl().getHost().compareTo(o.getUrl().getHost());
	}

	protected abstract URL doConfigure(URL currentUrl, URL configUrl);

	public static void main(String[] args) {
		System.out.println(URL.encode("timeout=100"));
	}

}
