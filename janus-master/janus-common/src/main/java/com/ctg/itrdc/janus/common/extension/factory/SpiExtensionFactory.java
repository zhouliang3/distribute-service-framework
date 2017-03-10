package com.ctg.itrdc.janus.common.extension.factory;

import com.ctg.itrdc.janus.common.extension.ExtensionFactory;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * SpiExtensionFactory
 * 
 * 基于Spi的工厂。通过ExtensionLoader查找到其adaptiveExtension实现对象实例
 * 
 * @author Administrator
 */
public class SpiExtensionFactory implements ExtensionFactory {

	public <T> T getExtension(Class<T> type, String name) {
		if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
			ExtensionLoader<T> loader = ExtensionLoader
					.getExtensionLoader(type);
			if (loader.getSupportedExtensions().size() > 0) {
				return loader.getAdaptiveExtension();
			}
		}
		return null;
	}
}
