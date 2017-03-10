package com.ctg.itrdc.janus.common.extensionloader.ext9_noadaptive;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 简单扩展点。 没有Wrapper。
 *
 * @author Administrator
 */
@SPI("impl1")
public interface Ext9 {
	// 无@Adaptive ！
	String echo(URL url, int i);
}