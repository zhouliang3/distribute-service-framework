/**
 * 
 */
package com.ctg.itrdc.janus.common.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;

/**
 * 接口的代理实现类生成器
 * 
 * 根据SPI的接口定义，生成代理实现类，该类的代码如下：<br/>
 * 类名为:接口的全名+$Adpative <br/>
 * 方法为接口中的各个方法中，如果方法未标识为@Adaptive的话，生成的方法抛出UnsupportedOperationException异常 <br/>
 * 如果方法标识了@Adaptive，那么根据相关参数，获取实际的实现类，然后做转发代理调用。
 * 
 * @author Administrator
 */
public final class AdaptiveClassGenerator {

	private static final Logger logger = LoggerFactory
			.getLogger(AdaptiveClassGenerator.class);

	/**
	 * 根据type，生成Adaptive扩展代理类代码
	 * 
	 * @param type
	 * @param defaultExtName
	 * @return
	 */
	public static String createAdaptiveExtensionClassCode(Class<?> type,
			String defaultExtName) {
		StringBuilder codeBuidler = new StringBuilder();
		Method[] methods = type.getMethods();
		assertHasAdaptiveAnnotation(type, methods);

		addPackageDeclaration(type, codeBuidler);
		addImportDeclaration(codeBuidler);
		addClassDeclarationStart(type, codeBuidler);
		addMethods(type, defaultExtName, codeBuidler, methods);
		addClassDeclarationEnd(codeBuidler);
		if (logger.isDebugEnabled()) {
			logger.debug(codeBuidler.toString());
		}
		return codeBuidler.toString();
	}

	/**
	 * 生成所有的方法声明代码
	 * 
	 * @param type
	 * @param defaultExtName
	 * @param codeBuidler
	 * @param methods
	 */
	private static void addMethods(Class<?> type, String defaultExtName,
			StringBuilder codeBuidler, Method[] methods) {
		for (Method method : methods) {
			Class<?> rt = method.getReturnType();
			Class<?>[] pts = method.getParameterTypes();

			addMethodDeclarationStart(codeBuidler, method, rt, pts);
			addMethodInnerCode(codeBuidler, type, defaultExtName, method, rt,
					pts);
			addMethodDeclarationEnd(codeBuidler);
		}
	}

	/**
	 * @param codeBuidler
	 */
	private static void addMethodDeclarationEnd(StringBuilder codeBuidler) {
		codeBuidler.append(" \n}");
	}

	/**
	 * @param codeBuidler
	 * @param method
	 * @param rt
	 * @param pts
	 */
	private static void addMethodDeclarationStart(StringBuilder codeBuidler,
			Method method, Class<?> rt, Class<?>[] pts) {
		Class<?>[] ets = method.getExceptionTypes();
		codeBuidler.append("\npublic " + rt.getCanonicalName() + " "
				+ method.getName() + "(");
		for (int i = 0; i < pts.length; i++) {
			if (i > 0) {
				codeBuidler.append(", ");
			}
			codeBuidler.append(pts[i].getCanonicalName());
			codeBuidler.append(" ");
			codeBuidler.append("arg" + i);
		}
		codeBuidler.append(")");
		if (ets.length > 0) {
			codeBuidler.append(" throws ");
			for (int i = 0; i < ets.length; i++) {
				if (i > 0) {
					codeBuidler.append(", ");
				}
				codeBuidler.append(ets[i].getCanonicalName());
			}
		}
		codeBuidler.append(" {");
	}

	/**
	 * @param type
	 * @param defaultExtName
	 * @param method
	 * @param rt
	 * @param pts
	 * @return
	 */
	private static void addMethodInnerCode(StringBuilder codeBuidler,
			Class<?> type, String defaultExtName, Method method, Class<?> rt,
			Class<?>[] pts) {
		Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
		if (adaptiveAnnotation == null) {
			codeBuidler
					.append("throw new UnsupportedOperationException(\"method ")
					.append(method.toString()).append(" of interface ")
					.append(type.getName())
					.append(" is not adaptive method!\");");
			return;
		}
		StringBuilder code = new StringBuilder(512);
		addUrlParamCode(type, method, pts, code);

		String[] value = adaptiveAnnotation.value();
		// 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
		if (value.length == 0) {
			char[] charArray = type.getSimpleName().toCharArray();
			StringBuilder sb = new StringBuilder(128);
			for (int i = 0; i < charArray.length; i++) {
				if (Character.isUpperCase(charArray[i])) {
					if (i != 0) {
						sb.append(".");
					}
					sb.append(Character.toLowerCase(charArray[i]));
				} else {
					sb.append(charArray[i]);
				}
			}
			value = new String[] { sb.toString() };
		}

		boolean hasInvocation = false;
		for (int i = 0; i < pts.length; ++i) {
			if (pts[i].getName().equals("com.ctg.itrdc.janus.rpc.Invocation")) {
				// Null Point check
				String s = String
						.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");",
								i);
				code.append(s);
				s = String.format(
						"\nString methodName = arg%d.getMethodName();", i);
				code.append(s);
				hasInvocation = true;
				break;
			}
		}

		String getNameCode = getNameCode(defaultExtName, value, hasInvocation);
		addProxyCallCode(type, method, rt, pts, code, value, getNameCode);
		codeBuidler.append(code.toString());
	}

	/**
	 * 生成extName的名字获取方式的表达式
	 * 
	 * @param defaultExtName
	 * @param value
	 * @param hasInvocation
	 * @return
	 */
	private static String getNameCode(String defaultExtName, String[] value,
			boolean hasInvocation) {
		String getNameCode = null;
		for (int i = value.length - 1; i >= 0; --i) {
			if (i == value.length - 1) {
				if (null != defaultExtName) {
					if (!"protocol".equals(value[i]))
						if (hasInvocation)
							getNameCode = String
									.format("url.getMethodParameter(methodName, \"%s\", \"%s\")",
											value[i], defaultExtName);
						else
							getNameCode = String.format(
									"url.getParameter(\"%s\", \"%s\")",
									value[i], defaultExtName);
					else
						getNameCode = String
								.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )",
										defaultExtName);
				} else {
					if (!"protocol".equals(value[i]))
						if (hasInvocation)
							getNameCode = String
									.format("url.getMethodParameter(methodName, \"%s\", \"%s\")",
											value[i], defaultExtName);
						else
							getNameCode = String.format(
									"url.getParameter(\"%s\")", value[i]);
					else
						getNameCode = "url.getProtocol()";
				}
			} else {
				if (!"protocol".equals(value[i]))
					if (hasInvocation)
						getNameCode = String
								.format("url.getMethodParameter(methodName, \"%s\", \"%s\")",
										value[i], defaultExtName);
					else
						getNameCode = String.format(
								"url.getParameter(\"%s\", %s)", value[i],
								getNameCode);
				else
					getNameCode = String
							.format("url.getProtocol() == null ? (%s) : url.getProtocol()",
									getNameCode);
			}
		}
		return getNameCode;
	}

	/**
	 * 增加代理调用的code
	 * 
	 * @param type
	 * @param method
	 * @param rt
	 * @param pts
	 * @param code
	 * @param value
	 * @param getNameCode
	 */
	private static void addProxyCallCode(Class<?> type, Method method,
			Class<?> rt, Class<?>[] pts, StringBuilder code, String[] value,
			String getNameCode) {
		code.append("\nString extName = ").append(getNameCode).append(";");
		// check extName == null?
		String s = String
				.format("\nif(extName == null) "
						+ "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
						type.getName(), Arrays.toString(value));
		code.append(s);

		s = String
				.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
						type.getName(), ExtensionLoader.class.getSimpleName(),
						type.getName());
		code.append(s);

		// return statement
		if (!rt.equals(void.class)) {
			code.append("\nreturn ");
		}

		s = String.format("extension.%s(", method.getName());
		code.append(s);
		for (int i = 0; i < pts.length; i++) {
			if (i != 0)
				code.append(", ");
			code.append("arg").append(i);
		}
		code.append(");");
	}

	/**
	 * 增加对参数url的获取的的代码
	 * 
	 * @param type
	 * @param method
	 * @param pts
	 * @param code
	 */
	private static void addUrlParamCode(Class<?> type, Method method,
			Class<?>[] pts, StringBuilder code) {
		int urlTypeIndex = getUrlTypeParamIndex(pts);
		// 有类型为URL的参数
		if (urlTypeIndex != -1) {
			// Null Point check
			String s = String
					.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
							urlTypeIndex);
			code.append(s);

			s = String.format("\n%s url = arg%d;", URL.class.getName(),
					urlTypeIndex);
			code.append(s);
		}
		// 参数没有URL类型
		else {
			String attribMethod = null;

			// 找到参数的URL属性
			LBL_PTS: for (int i = 0; i < pts.length; ++i) {
				Method[] ms = pts[i].getMethods();
				for (Method m : ms) {
					String name = m.getName();
					if ((name.startsWith("get") || name.length() > 3)
							&& Modifier.isPublic(m.getModifiers())
							&& !Modifier.isStatic(m.getModifiers())
							&& m.getParameterTypes().length == 0
							&& m.getReturnType() == URL.class) {
						urlTypeIndex = i;
						attribMethod = name;
						break LBL_PTS;
					}
				}
			}
			if (attribMethod == null) {
				throw new IllegalStateException(
						"fail to create adative class for interface "
								+ type.getName()
								+ ": not found url parameter or url attribute in parameters of method "
								+ method.getName());
			}

			// Null point check
			String s = String
					.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
							urlTypeIndex, pts[urlTypeIndex].getName());
			code.append(s);
			s = String
					.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
							urlTypeIndex, attribMethod,
							pts[urlTypeIndex].getName(), attribMethod);
			code.append(s);

			s = String.format("%s url = arg%d.%s();", URL.class.getName(),
					urlTypeIndex, attribMethod);
			code.append(s);
		}
	}

	/**
	 * 根据Class类型的数组，获取类型为URL的参数索引位置，从0开始
	 * 
	 * @param pts
	 * @return
	 */
	private static int getUrlTypeParamIndex(Class<?>[] pts) {
		int urlTypeIndex = -1;
		for (int i = 0; i < pts.length; ++i) {
			if (pts[i].equals(URL.class)) {
				urlTypeIndex = i;
				break;
			}
		}
		return urlTypeIndex;
	}

	/**
	 * 生成类声明结束标识代码
	 * 
	 * @param codeBuidler
	 */
	private static void addClassDeclarationEnd(StringBuilder codeBuidler) {
		codeBuidler.append("\n}");
	}

	/**
	 * 生成类声明开始标识代码
	 * 
	 * @param type
	 * @param codeBuidler
	 */
	private static void addClassDeclarationStart(Class<?> type,
			StringBuilder codeBuidler) {
		codeBuidler
				.append("\npublic class " + type.getSimpleName() + "$Adpative"
						+ " implements " + type.getCanonicalName() + " {");
	}

	/**
	 * @param codeBuidler
	 */
	private static void addImportDeclaration(StringBuilder codeBuidler) {
		codeBuidler.append("\nimport " + ExtensionLoader.class.getName() + ";");
	}

	/**
	 * 包声明
	 * 
	 * @param type
	 * @param codeBuidler
	 */
	private static void addPackageDeclaration(Class<?> type,
			StringBuilder codeBuidler) {
		codeBuidler.append("package " + type.getPackage().getName() + ";");
	}

	/**
	 * 对传入的type的所有方法进行判断，确保接口类型包含了Adaptive方法
	 * 
	 * 如果完全没有Adaptive方法，抛出IllegalStateException
	 * 
	 * @param type
	 * @param methods
	 */
	private static void assertHasAdaptiveAnnotation(Class<?> type,
			Method[] methods) {
		for (Method m : methods) {
			if (m.isAnnotationPresent(Adaptive.class)) {
				return;
			}
		}
		throw new IllegalStateException("No adaptive method on extension "
				+ type.getName() + ", refuse to create the adaptive class!");
	}

}
