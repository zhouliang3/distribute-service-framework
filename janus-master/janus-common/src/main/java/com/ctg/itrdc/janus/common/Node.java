package com.ctg.itrdc.janus.common;

/**
 * 结点
 * 
 * @author william.liangf
 */
public interface Node {

	/**
	 * 获取url
	 * 
	 * @return url.
	 */
	URL getUrl();

	/**
	 * 判断是否还有可用的
	 * 
	 * @return available.
	 */
	boolean isAvailable();

	/**
	 * 销毁
	 */
	void destroy();

}