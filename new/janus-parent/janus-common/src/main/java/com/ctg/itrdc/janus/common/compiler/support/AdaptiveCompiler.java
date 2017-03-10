package com.ctg.itrdc.janus.common.compiler.support;

import com.ctg.itrdc.janus.common.compiler.Compiler;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.common.utils.StringUtils;

/**
 * AdaptiveCompiler. (SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
@Adaptive
public class AdaptiveCompiler implements Compiler {

	private static volatile String DEFAULT_COMPILER;

	public static void setDefaultCompiler(String compiler) {
		DEFAULT_COMPILER = compiler;
	}

	/**
	 * 编译
	 */
	public Class<?> compile(String code, ClassLoader classLoader) {
		Compiler compiler;
		ExtensionLoader<Compiler> loader = ExtensionLoader
				.getExtensionLoader(Compiler.class);
		String name = DEFAULT_COMPILER; // copy reference
		if (StringUtils.isNotEmpty(name)) {
			compiler = loader.getExtension(name);
		} else {
			compiler = loader.getDefaultExtension();
		}
		return compiler.compile(code, classLoader);
	}

}
