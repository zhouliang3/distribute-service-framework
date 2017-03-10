package com.ctg.itrdc.janus.common.compiler.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ctg.itrdc.janus.common.compiler.Compiler;
import com.ctg.itrdc.janus.common.utils.ClassHelper;
import com.ctg.itrdc.janus.common.utils.StringUtils;

/**
 * 
 * 抽象编译类实现
 * 
 * 对各种编译类的公共逻辑进行抽取
 * 
 * @author Administrator
 */
public abstract class AbstractCompiler implements Compiler {

	private static final Pattern PACKAGE_PATTERN = Pattern
			.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

	private static final Pattern CLASS_PATTERN = Pattern
			.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

	public Class<?> compile(String code, ClassLoader classLoader) {
		code = code.trim();
		String className = getClassName(code);
		try {
			return Class.forName(className, true,
					ClassHelper.getCallerClassLoader(getClass()));
		} catch (ClassNotFoundException e) {
			if (!code.endsWith("}")) {
				throw new IllegalStateException(
						"The java code not endsWith \"}\", code: \n" + code
								+ "\n");
			}
			try {
				return doCompile(className, code);
			} catch (RuntimeException t) {
				throw t;
			} catch (Throwable t) {
				throw new IllegalStateException(
						"Failed to compile class, cause: " + t.getMessage()
								+ ", class: " + className + ", code: \n" + code
								+ "\n, stack: " + ClassUtils.toString(t));
			}
		}
	}

	/**
	 * 根据源代码，获取里面定义的类名。类名格式为fullname，即包名+类名
	 * 
	 * @param code
	 * @return
	 */
	private String getClassName(String code) {
		Matcher matcher = PACKAGE_PATTERN.matcher(code);
		String pkg = "";
		if (matcher.find()) {
			pkg = matcher.group(1);
		}
		matcher = CLASS_PATTERN.matcher(code);
		String cls;
		if (matcher.find()) {
			cls = matcher.group(1);
		} else {
			throw new IllegalArgumentException("No such class name in " + code);
		}

		return StringUtils.isNotEmpty(pkg) ? pkg + "." + cls : cls;
	}

	/**
	 * 处理编译工作
	 * 
	 * @param name
	 * @param source
	 * @return
	 * @throws Throwable
	 */
	protected abstract Class<?> doCompile(String name, String source)
			throws Throwable;

}
