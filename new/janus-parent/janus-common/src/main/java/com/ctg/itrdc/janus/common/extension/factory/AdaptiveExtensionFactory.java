package com.ctg.itrdc.janus.common.extension.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.ExtensionFactory;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;

/**
 * AdaptiveExtensionFactory
 * 
 * ExtensionFactory工厂的Adaptive实现，通过找到所有当前支持的ExtensionFactory实现，然后<br/>
 * 逐一对每个ExtensionFactory实现进行获取，直到获取到所需要的对象实例为止。如果找不到实例，返回null
 * 
 * 
 * @author Administrator
 */
@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {

	private final List<ExtensionFactory> factories;

	public AdaptiveExtensionFactory() {
		ExtensionLoader<ExtensionFactory> loader = ExtensionLoader
				.getExtensionLoader(ExtensionFactory.class);
		List<ExtensionFactory> list = new ArrayList<ExtensionFactory>();
		for (String name : loader.getSupportedExtensions()) {
			list.add(loader.getExtension(name));
		}
		factories = Collections.unmodifiableList(list);
	}

	public <T> T getExtension(Class<T> type, String name) {
		for (ExtensionFactory factory : factories) {
			T extension = factory.getExtension(type, name);
			if (extension != null) {
				return extension;
			}
		}
		return null;
	}

}
