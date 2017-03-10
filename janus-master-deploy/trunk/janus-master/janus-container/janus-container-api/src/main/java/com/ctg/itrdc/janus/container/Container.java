package com.ctg.itrdc.janus.container;

import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 组件容器
 * 
 * @author Administrator
 */
@SPI("spring")
public interface Container {

	/**
	 * 开始
	 */
	void start();

	/**
	 * 结束
	 */
	void stop();

}