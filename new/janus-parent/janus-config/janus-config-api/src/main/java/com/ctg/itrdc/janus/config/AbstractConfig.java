package com.ctg.itrdc.janus.config;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.CollectionUtils;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.ReflectUtils;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.config.support.Parameter;

/**
 * 配置解析的工具方法、公共方法
 * 
 * @author Administrator
 * @export
 */
public abstract class AbstractConfig implements Serializable {

	private static final long serialVersionUID = 4267533505537413570L;

	protected static final Logger logger = LoggerFactory
			.getLogger(AbstractConfig.class);

	/**
	 * 最大长度
	 */
	private static final int MAX_LENGTH = 100;

	/**
	 * 路径的最大长度
	 */
	private static final int MAX_PATH_LENGTH = 200;

	/**
	 * 配置名字的合法格式
	 */
	private static final Pattern PATTERN_NAME = Pattern
			.compile("[\\-._0-9a-zA-Z]+");

	/**
	 * 包含一个或者多个名字的合法格式
	 */
	private static final Pattern PATTERN_MULTI_NAME = Pattern
			.compile("[,\\-._0-9a-zA-Z]+");

	/**
	 * 方法名字的合法格式
	 */
	private static final Pattern PATTERN_METHOD_NAME = Pattern
			.compile("[a-zA-Z][0-9a-zA-Z]*");

	/**
	 * 路径的合法格式
	 */
	private static final Pattern PATTERN_PATH = Pattern
			.compile("[/\\-$._0-9a-zA-Z]+");

	/**
	 * 包含符号的合法格式
	 */
	private static final Pattern PATTERN_NAME_HAS_SYMBOL = Pattern
			.compile("[:*,/\\-._0-9a-zA-Z]+");

	/**
	 * 关键字的合法格式
	 */
	private static final Pattern PATTERN_KEY = Pattern
			.compile("[*,\\-._0-9a-zA-Z]+");

	protected String id;

	@Parameter(excluded = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 增加注解中的属性。将注解中的属性值，设置到本对象中的属性中
	 * 
	 * @param annotationClass
	 * @param annotation
	 */
	protected void appendAnnotation(Class<?> annotationClass, Object annotation) {
		Method[] methods = annotationClass.getMethods();
		for (Method method : methods) {
			if (isEffectiveMethod(method)) {
				continue;
			}
			try {
				String property = method.getName();
				if ("interfaceClass".equals(property)
						|| "interfaceName".equals(property)) {
					property = "interface";
				}
				Object value = method.invoke(annotation, new Object[0]);
				if (value != null && !value.equals(method.getDefaultValue())) {
					setPropertyValue(method, property, value);
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 设置属性值
	 * 
	 * @param method
	 * @param property
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void setPropertyValue(Method method, String property, Object value)
			throws IllegalAccessException, InvocationTargetException {
		Class<?> parameterType = ReflectUtils.getBoxedClass(method
				.getReturnType());
		if ("filter".equals(property) || "listener".equals(property)) {
			parameterType = String.class;
			value = StringUtils.join((String[]) value, ",");
		} else if ("parameters".equals(property)) {
			parameterType = Map.class;
			value = CollectionUtils.toStringMap((String[]) value);
		}
		try {
			String setter = "set" + property.substring(0, 1).toUpperCase()
					+ property.substring(1);
			Method setterMethod = getClass().getMethod(setter,
					new Class<?>[] { parameterType });
			setterMethod.invoke(this, new Object[] { value });
		} catch (NoSuchMethodException e) {
			// ignore
		}
	}

	/**
	 * 根据一个方法，判断该方法是否有效的获取值的方法
	 * 
	 * @param method
	 * @return
	 */
	private boolean isEffectiveMethod(Method method) {
		return method.getDeclaringClass() == Object.class
				|| method.getReturnType() == void.class
				|| method.getParameterTypes().length != 0
				|| !Modifier.isPublic(method.getModifiers())
				|| Modifier.isStatic(method.getModifiers());
	}

	/**
	 * 添加config的配置属性。读取config的每个set方法，未每个属性从系统属性读取，或者从全局的配置读取
	 * 
	 * 
	 * @param config
	 */
	protected static void appendProperties(AbstractConfig config) {
		if (config == null) {
			return;
		}
		String prefix = "janus." + getTagName(config.getClass()) + ".";
		Method[] methods = config.getClass().getMethods();
		for (Method method : methods) {
			appendSingleProperty(config, prefix, method);
		}
	}

	/**
	 * @param config
	 * @param prefix
	 * @param method
	 */
	private static void appendSingleProperty(AbstractConfig config,
			String prefix, Method method) {
		try {
			String name = method.getName();
			if (name.length() > 3 && name.startsWith("set")
					&& Modifier.isPublic(method.getModifiers())
					&& method.getParameterTypes().length == 1
					&& isPrimitive(method.getParameterTypes()[0])) {
				String value = getPropertyValue(config, prefix, name);
				if (value != null && value.length() > 0) {
					method.invoke(
							config,
							new Object[] { convertPrimitive(
									method.getParameterTypes()[0], value) });
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 根据指定属性名字，获取该属性的配置值
	 * 
	 * 以ReferenceConfig为例，配置属性按照下面的顺序获取<br/>
	 * 1.如果config有id，就根据janus.reference.${configId}.${propertyName} 从系统属性获取<br/>
	 * 2.就根据janus.reference.${propertyName} 从系统属性获取 <br/>
	 * 3.根据get方法，获取config的值如果获取不到值，根据ConfigUtils.getProperty(janus.reference.${
	 * configId}.${propertyName})读取属性<br/>
	 * 4.根据ConfigUtils.getProperty(janus.reference.${propertyName})读取属性<br/>
	 * 
	 * @param config
	 * @param prefix
	 * @param name
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static String getPropertyValue(AbstractConfig config,
			String prefix, String name) throws IllegalAccessException,
			InvocationTargetException {
		String property = StringUtils.camelToSplitName(name.substring(3, 4)
				.toLowerCase() + name.substring(4), "-");
		String value = null;
		if (config.getId() != null && config.getId().length() > 0) {
			String pn = prefix + config.getId() + "." + property;
			value = System.getProperty(pn);
			if (!StringUtils.isBlank(value)) {
				logger.info("Use System Property " + pn + " to config janus");
				return value;
			}
		}
		String pn = prefix + property;
		value = System.getProperty(pn);
		if (!StringUtils.isBlank(value)) {
			logger.info("Use System Property " + pn + " to config janus");
			return value;
		}
		Method getter = getGetterMethod(config, name);
		if (getter != null && getter.invoke(config, new Object[0]) == null) {
			if (config.getId() != null && config.getId().length() > 0) {
				value = ConfigUtils.getProperty(prefix + config.getId() + "."
						+ property);
			}
			if (value == null || value.length() == 0) {
				value = ConfigUtils.getProperty(prefix + property);
			}
		}
		return value;
	}

	/**
	 * 获取get方法
	 * 
	 * @param config
	 * @param name
	 * @return
	 */
	private static Method getGetterMethod(AbstractConfig config, String name) {
		Method getter;
		try {
			getter = config.getClass().getMethod("get" + name.substring(3),
					new Class<?>[0]);
		} catch (NoSuchMethodException e) {
			try {
				getter = config.getClass().getMethod("is" + name.substring(3),
						new Class<?>[0]);
			} catch (NoSuchMethodException e2) {
				getter = null;
			}
		}
		return getter;
	}

	/**
	 * 根据Class，获取该class的tag名称。
	 * 
	 * 例如对于ReferenceConfig，返回reference;对于ServiceBean，返回service
	 * 
	 * @param cls
	 * @return
	 */
	private static String getTagName(Class<?> cls) {
		String tag = cls.getSimpleName();
		for (String suffix : SUFFIXS) {
			if (tag.endsWith(suffix)) {
				tag = tag.substring(0, tag.length() - suffix.length());
				break;
			}
		}
		tag = tag.toLowerCase();
		return tag;
	}

	/**
	 * 将config中的get或者is，以及getParameters方法得到的参数，取值并填充到parameters中
	 * 
	 * @param parameters
	 * @param config
	 */
	protected static void appendParameters(Map<String, String> parameters,
			Object config) {
		appendParameters(parameters, config, null);
	}

	/**
	 * 将config中的get或者is，以及getParameters方法得到的参数，取值并填充到parameters中
	 * 
	 * @param parameters
	 * @param config
	 * @param prefix
	 */
	@SuppressWarnings("unchecked")
	protected static void appendParameters(Map<String, String> parameters,
			Object config, String prefix) {
		if (config == null) {
			return;
		}
		Method[] methods = config.getClass().getMethods();
		for (Method method : methods) {
			try {
				String name = method.getName();
				if ((name.startsWith("get") || name.startsWith("is"))
						&& !"getClass".equals(name)
						&& Modifier.isPublic(method.getModifiers())
						&& method.getParameterTypes().length == 0
						&& isPrimitive(method.getReturnType())) {
					Parameter parameter = method.getAnnotation(Parameter.class);
					if (method.getReturnType() == Object.class
							|| parameter != null && parameter.excluded()) {
						continue;
					}
					appendByCommonGetMethod(parameters, name, parameter,
							method, config, prefix);
				} else if ("getParameters".equals(name)
						&& Modifier.isPublic(method.getModifiers())
						&& method.getParameterTypes().length == 0
						&& method.getReturnType() == Map.class) {
					appendByGetParametersMethod(parameters, config, prefix,
							method);
				}
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

	/**
	 * 根据普通的get或者is方法，进行参数的解析以及设置
	 * 
	 * @param parameters
	 * @param name
	 * @param parameter
	 * @param method
	 * @param config
	 * @param prefix
	 * @throws Exception
	 */
	private static void appendByCommonGetMethod(Map<String, String> parameters,
			String name, Parameter parameter, Method method, Object config,
			String prefix) throws Exception {
		int i = name.startsWith("get") ? 3 : 2;
		String prop = StringUtils.camelToSplitName(name.substring(i, i + 1)
				.toLowerCase() + name.substring(i + 1), ".");
		String key;
		if (parameter != null && parameter.key() != null
				&& parameter.key().length() > 0) {
			key = parameter.key();
		} else {
			key = prop;
		}
		Object value = method.invoke(config, new Object[0]);
		String str = String.valueOf(value).trim();
		if (value != null && str.length() > 0) {
			if (parameter != null && parameter.escaped()) {
				str = URL.encode(str);
			}
			if (parameter != null && parameter.append()) {
				String pre = (String) parameters.get(Constants.DEFAULT_KEY
						+ "." + key);
				if (pre != null && pre.length() > 0) {
					str = pre + "," + str;
				}
				pre = (String) parameters.get(key);
				if (pre != null && pre.length() > 0) {
					str = pre + "," + str;
				}
			}
			if (prefix != null && prefix.length() > 0) {
				key = prefix + "." + key;
			}
			parameters.put(key, str);
		} else if (parameter != null && parameter.required()) {
			throw new IllegalStateException(config.getClass().getSimpleName()
					+ "." + key + " == null");
		}
	}

	/**
	 * 根据getParameters方法，进行参数的解析以及设置
	 * 
	 * @param parameters
	 * @param config
	 * @param prefix
	 * @param method
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void appendByGetParametersMethod(
			Map<String, String> parameters, Object config, String prefix,
			Method method) throws IllegalAccessException,
			InvocationTargetException {
		Map<String, String> map = (Map<String, String>) method.invoke(config,
				new Object[0]);
		if (map != null && map.size() > 0) {
			String pre = (prefix != null && prefix.length() > 0 ? prefix + "."
					: "");
			for (Map.Entry<String, String> entry : map.entrySet()) {
				parameters.put(pre + entry.getKey().replace('-', '.'),
						entry.getValue());
			}
		}
	}

	/**
	 * 设置基本属性
	 * 
	 * @param parameters
	 * @param config
	 */
	protected static void appendAttributes(Map<Object, Object> parameters,
			AbstractConfig config) {
		appendAttributes(parameters, config, null);
	}

	protected static void appendAttributes(Map<Object, Object> parameters,
			AbstractConfig config, String prefix) {
		if (config == null) {
			return;
		}
		Method[] methods = config.getClass().getMethods();
		for (Method method : methods) {
			try {
				String name = method.getName();
				if ((name.startsWith("get") || name.startsWith("is"))
						&& !"getClass".equals(name)
						&& Modifier.isPublic(method.getModifiers())
						&& method.getParameterTypes().length == 0
						&& isPrimitive(method.getReturnType())) {
					Parameter parameter = method.getAnnotation(Parameter.class);
					if (parameter == null || !parameter.attribute())
						continue;
					String key;
					if (parameter != null && parameter.key() != null
							&& parameter.key().length() > 0) {
						key = parameter.key();
					} else {
						int i = name.startsWith("get") ? 3 : 2;
						key = name.substring(i, i + 1).toLowerCase()
								+ name.substring(i + 1);
					}
					Object value = method.invoke(config, new Object[0]);
					if (value != null) {
						if (prefix != null && prefix.length() > 0) {
							key = prefix + "." + key;
						}
						parameters.put(key, value);
					}
				}
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

	private static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive() || type == String.class
				|| type == Character.class || type == Boolean.class
				|| type == Byte.class || type == Short.class
				|| type == Integer.class || type == Long.class
				|| type == Float.class || type == Double.class
				|| type == Object.class;
	}

	/**
	 * 将基本类型的值转换成对应的对象
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	private static Object convertPrimitive(Class<?> type, String value) {
		if (type == char.class || type == Character.class) {
			return value.length() > 0 ? value.charAt(0) : '\0';
		} else if (type == boolean.class || type == Boolean.class) {
			return Boolean.valueOf(value);
		} else if (type == byte.class || type == Byte.class) {
			return Byte.valueOf(value);
		} else if (type == short.class || type == Short.class) {
			return Short.valueOf(value);
		} else if (type == int.class || type == Integer.class) {
			return Integer.valueOf(value);
		} else if (type == long.class || type == Long.class) {
			return Long.valueOf(value);
		} else if (type == float.class || type == Float.class) {
			return Float.valueOf(value);
		} else if (type == double.class || type == Double.class) {
			return Double.valueOf(value);
		}
		return value;
	}

	/**
	 * 检查根据value属性，是否能找到属于type类型的Extension
	 * 
	 * @param type
	 * @param property
	 * @param value
	 */
	protected static void checkExtension(Class<?> type, String property,
			String value) {
		checkName(property, value);
		if (value != null
				&& value.length() > 0
				&& !ExtensionLoader.getExtensionLoader(type)
						.hasExtension(value)) {
			throw new IllegalStateException("No such extension " + value
					+ " for " + property + "/" + type.getName());
		}
	}

	protected static void checkMultiExtension(Class<?> type, String property,
			String value) {
		checkMultiName(property, value);
		if (value != null && value.length() > 0) {
			String[] values = value.split("\\s*[,]+\\s*");
			for (String v : values) {
				if (v.startsWith(Constants.REMOVE_VALUE_PREFIX)) {
					v = v.substring(1);
				}
				if (Constants.DEFAULT_KEY.equals(v)) {
					continue;
				}
				if (!ExtensionLoader.getExtensionLoader(type).hasExtension(v)) {
					throw new IllegalStateException("No such extension " + v
							+ " for " + property + "/" + type.getName());
				}
			}
		}
	}

	protected static void checkLength(String property, String value) {
		checkProperty(property, value, MAX_LENGTH, null);
	}

	protected static void checkPathLength(String property, String value) {
		checkProperty(property, value, MAX_PATH_LENGTH, null);
	}

	protected static void checkName(String property, String value) {
		checkProperty(property, value, MAX_LENGTH, PATTERN_NAME);
	}

	protected static void checkNameHasSymbol(String property, String value) {
		checkProperty(property, value, MAX_LENGTH, PATTERN_NAME_HAS_SYMBOL);
	}

	protected static void checkKey(String property, String value) {
		checkProperty(property, value, MAX_LENGTH, PATTERN_KEY);
	}

	protected static void checkMultiName(String property, String value) {
		checkProperty(property, value, MAX_LENGTH, PATTERN_MULTI_NAME);
	}

	protected static void checkPathName(String property, String value) {
		checkProperty(property, value, MAX_PATH_LENGTH, PATTERN_PATH);
	}

	protected static void checkMethodName(String property, String value) {
		checkProperty(property, value, MAX_LENGTH, PATTERN_METHOD_NAME);
	}

	/**
	 * 检查每个参数的名字
	 * 
	 * @param parameters
	 */
	protected static void checkParameterName(Map<String, String> parameters) {
		if (parameters == null || parameters.size() == 0) {
			return;
		}
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			// change by tony.chenl parameter value maybe has colon.for example
			// napoli address
			checkNameHasSymbol(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 属性校验公共方法
	 * 
	 * @param property
	 * @param value
	 * @param maxlength
	 * @param pattern
	 */
	protected static void checkProperty(String property, String value,
			int maxlength, Pattern pattern) {
		if (value == null || value.length() == 0) {
			return;
		}
		if (value.length() > maxlength) {
			throw new IllegalStateException("Invalid " + property + "=\""
					+ value + "\" is longer than " + maxlength);
		}
		if (pattern != null) {
			Matcher matcher = pattern.matcher(value);
			if (!matcher.matches()) {
				throw new IllegalStateException(
						"Invalid "
								+ property
								+ "=\""
								+ value
								+ "\" contain illegal charactor, only digit, letter, '-', '_' and '.' is legal.");
			}
		}
	}

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if (logger.isInfoEnabled()) {
					logger.info("Run shutdown hook now.");
				}
				ProtocolConfig.destroyAll();
			}
		}, "JanusShutdownHook"));
	}

	private static final String[] SUFFIXS = new String[] { "Config", "Bean" };

	@Override
	public String toString() {
		try {
			StringBuilder buf = new StringBuilder();
			buf.append("<janus:");
			buf.append(getTagName(getClass()));
			Method[] methods = getClass().getMethods();
			for (Method method : methods) {
				try {
					String name = method.getName();
					if ((name.startsWith("get") || name.startsWith("is"))
							&& !"getClass".equals(name) && !"get".equals(name)
							&& !"is".equals(name)
							&& Modifier.isPublic(method.getModifiers())
							&& method.getParameterTypes().length == 0
							&& isPrimitive(method.getReturnType())) {
						int i = name.startsWith("get") ? 3 : 2;
						String key = name.substring(i, i + 1).toLowerCase()
								+ name.substring(i + 1);
						Object value = method.invoke(this, new Object[0]);
						if (value != null) {
							buf.append(" ");
							buf.append(key);
							buf.append("=\"");
							buf.append(value);
							buf.append("\"");
						}
					}
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
			buf.append(" />");
			return buf.toString();
		} catch (Throwable t) { // 防御性容错
			logger.warn(t.getMessage(), t);
			return super.toString();
		}
	}

}