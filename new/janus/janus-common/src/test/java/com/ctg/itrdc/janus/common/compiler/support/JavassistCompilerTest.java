package com.ctg.itrdc.janus.common.compiler.support;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import org.junit.Test;

import com.ctg.itrdc.janus.common.compiler.Compiler;
import com.ctg.itrdc.janus.common.utils.ClassHelper;

/**
 * @author Administrator
 */
public class JavassistCompilerTest extends BaseCompilerTest {

	JavassistCompiler compiler = new JavassistCompiler();

	public void before() {

		ClassPool pool = new ClassPool(true);
		pool.appendClassPath(new LoaderClassPath(ClassHelper
				.getCallerClassLoader(getClass())));
	}

	@Override
	protected Compiler getCompiler() {
		return compiler;
	}

	@Test
	public void testErrorClass() throws Exception {
		try {
			String classContent = readErrorClassContent();
			getCompiler().compile(classContent,
					JdkCompiler.class.getClassLoader());
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(),
					allOf(containsString("Failed to compile class")));
		}
	}
}
