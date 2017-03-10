package com.ctg.itrdc.janus.common.compiler.support;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import junit.framework.TestCase;

import com.ctg.itrdc.janus.common.utils.IOUtils;
import com.ctg.itrdc.janus.common.compiler.Compiler;

public abstract class BaseCompilerTest extends TestCase {

	private static final String BASE_FILE_PATH = "./src/test/java/com/ctg/itrdc/janus/common/compiler/support/";

	private static final String NO_CLASS_NAME_FILE = "NoClassName.java.txt";

	private static final String NO_END_BRACKET_FILE = "NoEndBracket.java.txt";

	private static final String ERROR_CLASS = "ErrorSource.java.txt";

	private static final String CLASS_FILE_PATH = "TestSource.java.txt";

	protected static String readClassContent() throws Exception {
		File file = new File(BASE_FILE_PATH + CLASS_FILE_PATH);
		return IOUtils.read(new FileReader(file));
	}

	protected static String readNoClassNameContent() throws Exception {
		File file = new File(BASE_FILE_PATH + NO_CLASS_NAME_FILE);
		return IOUtils.read(new FileReader(file));
	}

	protected static String readNoEndBracketContent() throws Exception {
		File file = new File(BASE_FILE_PATH + NO_END_BRACKET_FILE);
		return IOUtils.read(new FileReader(file));
	}

	protected static String readErrorClassContent() throws Exception {
		File file = new File(BASE_FILE_PATH + ERROR_CLASS);
		return IOUtils.read(new FileReader(file));
	}

	@Test
	public void testCompile() throws Exception {
		String classContent = readClassContent();
		Class clazz = getCompiler().compile(classContent,
				JdkCompiler.class.getClassLoader());
		assertEquals(clazz.getName(),
				"com.ctg.itrdc.janus.common.compiler.support.TestSource");
	}

	@Test
	public void testNoClassName() throws Exception {
		try {
			String classContent = readNoClassNameContent();
			getCompiler().compile(classContent,
					JdkCompiler.class.getClassLoader());
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(),
					allOf(containsString("No such class name in")));
		}
	}

	@Test
	public void testNoEndBracket() throws Exception {
		try {
			String classContent = readNoEndBracketContent();
			System.out.println(classContent);
			getCompiler().compile(classContent,
					JdkCompiler.class.getClassLoader());
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(),
					allOf(containsString("The java code not endsWith")));
		}
	}

	protected abstract Compiler getCompiler();

}
