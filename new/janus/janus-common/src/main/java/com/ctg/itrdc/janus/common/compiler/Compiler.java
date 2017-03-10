package com.ctg.itrdc.janus.common.compiler;

import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 动态源代码编译接口
 * 
 * @author Administrator
 */
@SPI("javassist")
public interface Compiler {

	/**
	 * Compile java source code.
	 * 
	 * @param code
	 *            Java source code
	 * @param classLoader
	 *            TODO
	 * @return Compiled class
	 */
	Class<?> compile(String code, ClassLoader classLoader);

}
