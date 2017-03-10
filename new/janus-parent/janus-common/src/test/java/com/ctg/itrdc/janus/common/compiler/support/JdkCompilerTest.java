package com.ctg.itrdc.janus.common.compiler.support;

import com.ctg.itrdc.janus.common.compiler.Compiler;

/**
 * @author Administrator
 */
public class JdkCompilerTest extends BaseCompilerTest {

	JdkCompiler compiler = new JdkCompiler();

	@Override
	protected Compiler getCompiler() {
		return compiler;
	}
}
