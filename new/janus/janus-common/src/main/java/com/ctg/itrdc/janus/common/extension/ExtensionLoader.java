package com.ctg.itrdc.janus.common.extension;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.support.ActivateComparator;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.ConcurrentHashSet;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.Holder;
import com.ctg.itrdc.janus.common.utils.StringUtils;

/**
 * Java Spi的实现
 * 
 * 通过加入扩展点的形式，提供运行时的类绑定
 * 
 * @author Administrator
 */
public class ExtensionLoader<T> {

	private static final Logger logger = LoggerFactory
			.getLogger(ExtensionLoader.class);

	private static final String SERVICES_DIRECTORY = "META-INF/services/";

	private static final String JANUS_DIRECTORY = "META-INF/janus/";

	private static final String JANUS_INTERNAL_DIRECTORY = JANUS_DIRECTORY + "internal/";

	private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

	private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

	private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

	// ==============================

	private final Class<?> type;

	private final ExtensionFactory objectFactory;

	private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

	private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();

	private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();

	private volatile Class<?> cachedAdaptiveClass = null;

	private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();

	private String cachedDefaultName;

	private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();
	private volatile Throwable createAdaptiveInstanceError;

	private Set<Class<?>> cachedWrapperClasses;

	private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

	private static <T> boolean withExtensionAnnotation(Class<T> type) {
		return type.isAnnotationPresent(SPI.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
		if (type == null)
			throw new IllegalArgumentException("Extension type == null");
		if (!type.isInterface()) {
			throw new IllegalArgumentException("Extension type(" + type
					+ ") is not interface!");
		}
		if (!withExtensionAnnotation(type)) {
			throw new IllegalArgumentException("Extension type(" + type
					+ ") is not extension, because WITHOUT @"
					+ SPI.class.getSimpleName() + " Annotation!");
		}

		ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS
				.get(type);
		if (loader == null) {
			EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
			loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		}
		return loader;
	}

	private ExtensionLoader(Class<?> type) {
		this.type = type;
		objectFactory = (type == ExtensionFactory.class ? null
				: ExtensionLoader.getExtensionLoader(ExtensionFactory.class)
						.getAdaptiveExtension());
	}

	public String getExtensionName(T extensionInstance) {
		return getExtensionName(extensionInstance.getClass());
	}

	public String getExtensionName(Class<?> extensionClass) {
		return cachedNames.get(extensionClass);
	}

	/**
	 * This is equivalent to
	 * 
	 * <pre>
	 * getActivateExtension(url, key, null);
	 * </pre>
	 *
	 * @param url
	 *            url
	 * @param key
	 *            url parameter key which used to get extension point names
	 * @return extension list which are activated.
	 * @see #getActivateExtension(com.ctg.itrdc.janus.common.URL, String, String)
	 */
	public List<T> getActivateExtension(URL url, String key) {
		return getActivateExtension(url, key, null);
	}

	/**
	 * This is equivalent to
	 * 
	 * <pre>
	 * getActivateExtension(url, values, null);
	 * </pre>
	 *
	 * @see #getActivateExtension(com.ctg.itrdc.janus.common.URL, String[],
	 *      String)
	 * @param url
	 *            url
	 * @param values
	 *            extension point names
	 * @return extension list which are activated
	 */
	public List<T> getActivateExtension(URL url, String[] values) {
		return getActivateExtension(url, values, null);
	}

	/**
	 * This is equivalent to
	 * 
	 * <pre>
	 * getActivateExtension(url, url.getParameter(key).split(&quot;,&quot;), null);
	 * </pre>
	 *
	 * @see #getActivateExtension(com.ctg.itrdc.janus.common.URL, String[],
	 *      String)
	 * @param url
	 *            url
	 * @param key
	 *            url parameter key which used to get extension point names
	 * @param group
	 *            group
	 * @return extension list which are activated.
	 */
	public List<T> getActivateExtension(URL url, String key, String group) {
		String value = url.getParameter(key);
		return getActivateExtension(url,
				value == null || value.length() == 0 ? null
						: Constants.COMMA_SPLIT_PATTERN.split(value), group);
	}

	/**
	 * Get activate extensions.
	 *
	 * @see com.ctg.itrdc.janus.common.extension.Activate
	 * @param url
	 *            url
	 * @param values
	 *            extension point names
	 * @param group
	 *            group
	 * @return extension list which are activated
	 */
	public List<T> getActivateExtension(URL url, String[] values, String group) {
		List<T> exts = new ArrayList<T>();
		List<String> names = values == null ? new ArrayList<String>(0) : Arrays
				.asList(values);
		if (!names.contains(Constants.REMOVE_VALUE_PREFIX
				+ Constants.DEFAULT_KEY)) {
			getExtensionClasses();
			for (Map.Entry<String, Activate> entry : cachedActivates.entrySet()) {
				String name = entry.getKey();
				Activate activate = entry.getValue();
				if (isMatchGroup(group, activate.group())) {
					T ext = getExtension(name);
					if (!names.contains(name)
							&& !names.contains(Constants.REMOVE_VALUE_PREFIX
									+ name) && isActive(activate, url)) {
						exts.add(ext);
					}
				}
			}
			Collections.sort(exts, ActivateComparator.COMPARATOR);
		}
		List<T> usrs = new ArrayList<T>();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (!name.startsWith(Constants.REMOVE_VALUE_PREFIX)
					&& !names.contains(Constants.REMOVE_VALUE_PREFIX + name)) {
				if (Constants.DEFAULT_KEY.equals(name)) {
					if (usrs.size() > 0) {
						exts.addAll(0, usrs);
						usrs.clear();
					}
				} else {
					T ext = getExtension(name);
					usrs.add(ext);
				}
			}
		}
		if (usrs.size() > 0) {
			exts.addAll(usrs);
		}
		return exts;
	}

	private boolean isMatchGroup(String group, String[] groups) {
		if (group == null || group.length() == 0) {
			return true;
		}
		if (groups != null && groups.length > 0) {
			for (String g : groups) {
				if (group.equals(g)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isActive(Activate activate, URL url) {
		String[] keys = activate.value();
		if (keys == null || keys.length == 0) {
			return true;
		}
		for (String key : keys) {
			for (Map.Entry<String, String> entry : url.getParameters()
					.entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if ((k.equals(key) || k.endsWith("." + key))
						&& ConfigUtils.isNotEmpty(v)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 返回扩展点实例，如果没有指定的扩展点或是还没加载（即实例化）则返回<code>null</code>。注意：此方法不会触发扩展点的加载。
	 * <p />
	 * 一般应该调用{@link #getExtension(String)}方法获得扩展，这个方法会触发扩展点加载。
	 *
	 * @see #getExtension(String)
	 */
	@SuppressWarnings("unchecked")
	public T getLoadedExtension(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Extension name == null");
		Holder<Object> holder = cachedInstances.get(name);
		if (holder == null) {
			cachedInstances.putIfAbsent(name, new Holder<Object>());
			holder = cachedInstances.get(name);
		}
		return (T) holder.get();
	}

	/**
	 * 返回已经加载的扩展点的名字。
	 * <p />
	 * 一般应该调用{@link #getSupportedExtensions()}方法获得扩展，这个方法会返回所有的扩展点。
	 *
	 * @see #getSupportedExtensions()
	 */
	public Set<String> getLoadedExtensions() {
		return Collections.unmodifiableSet(new TreeSet<String>(cachedInstances
				.keySet()));
	}

	/**
	 * 返回指定名字的扩展。如果指定名字的扩展不存在，则抛异常 {@link IllegalStateException}.
	 *
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getExtension(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Extension name == null");
		if ("true".equals(name)) {
			return getDefaultExtension();
		}
		Holder<Object> holder = cachedInstances.get(name);
		if (holder == null) {
			cachedInstances.putIfAbsent(name, new Holder<Object>());
			holder = cachedInstances.get(name);
		}
		Object instance = holder.get();
		if (instance == null) {
			synchronized (holder) {
				instance = holder.get();
				if (instance == null) {
					instance = createExtension(name);
					holder.set(instance);
				}
			}
		}
		return (T) instance;
	}

	/**
	 * 返回缺省的扩展，如果没有设置则返回<code>null</code>。
	 */
	public T getDefaultExtension() {
		getExtensionClasses();
		if (null == cachedDefaultName || cachedDefaultName.length() == 0
				|| "true".equals(cachedDefaultName)) {
			return null;
		}
		return getExtension(cachedDefaultName);
	}

	public boolean hasExtension(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Extension name == null");
		try {
			return getExtensionClass(name) != null;
		} catch (Throwable t) {
			return false;
		}
	}

	public Set<String> getSupportedExtensions() {
		Map<String, Class<?>> clazzes = getExtensionClasses();
		return Collections
				.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
	}

	/**
	 * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。
	 */
	public String getDefaultExtensionName() {
		getExtensionClasses();
		return cachedDefaultName;
	}

	/**
	 * 编程方式添加新扩展点。
	 *
	 * @param name
	 *            扩展点名
	 * @param clazz
	 *            扩展点类
	 * @throws IllegalStateException
	 *             要添加扩展点名已经存在。
	 */
	public void addExtension(String name, Class<?> clazz) {
		getExtensionClasses(); // load classes

		if (!type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Input type " + clazz
					+ "not implement Extension " + type);
		}
		if (clazz.isInterface()) {
			throw new IllegalStateException("Input type " + clazz
					+ "can not be interface!");
		}

		if (!clazz.isAnnotationPresent(Adaptive.class)) {
			if (StringUtils.isBlank(name)) {
				throw new IllegalStateException(
						"Extension name is blank (Extension " + type + ")!");
			}
			if (cachedClasses.get().containsKey(name)) {
				throw new IllegalStateException("Extension name " + name
						+ " already existed(Extension " + type + ")!");
			}

			cachedNames.put(clazz, name);
			cachedClasses.get().put(name, clazz);
		} else {
			if (cachedAdaptiveClass != null) {
				throw new IllegalStateException(
						"Adaptive Extension already existed(Extension " + type
								+ ")!");
			}

			cachedAdaptiveClass = clazz;
		}
	}

	/**
	 * 编程方式添加替换已有扩展点。
	 *
	 * @param name
	 *            扩展点名
	 * @param clazz
	 *            扩展点类
	 * @throws IllegalStateException
	 *             要添加扩展点名已经存在。
	 * @deprecated 不推荐应用使用，一般只在测试时可以使用
	 */
	@Deprecated
	public void replaceExtension(String name, Class<?> clazz) {
		getExtensionClasses(); // load classes

		if (!type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Input type " + clazz
					+ "not implement Extension " + type);
		}
		if (clazz.isInterface()) {
			throw new IllegalStateException("Input type " + clazz
					+ "can not be interface!");
		}

		if (!clazz.isAnnotationPresent(Adaptive.class)) {
			if (StringUtils.isBlank(name)) {
				throw new IllegalStateException(
						"Extension name is blank (Extension " + type + ")!");
			}
			if (!cachedClasses.get().containsKey(name)) {
				throw new IllegalStateException("Extension name " + name
						+ " not existed(Extension " + type + ")!");
			}

			cachedNames.put(clazz, name);
			cachedClasses.get().put(name, clazz);
			cachedInstances.remove(name);
		} else {
			if (cachedAdaptiveClass == null) {
				throw new IllegalStateException(
						"Adaptive Extension not existed(Extension " + type
								+ ")!");
			}

			cachedAdaptiveClass = clazz;
			cachedAdaptiveInstance.set(null);
		}
	}

	@SuppressWarnings("unchecked")
	public T getAdaptiveExtension() {
		Object instance = cachedAdaptiveInstance.get();
		if (instance == null) {
			if (createAdaptiveInstanceError == null) {
				synchronized (cachedAdaptiveInstance) {
					instance = cachedAdaptiveInstance.get();
					if (instance == null) {
						try {
							instance = createAdaptiveExtension();
							cachedAdaptiveInstance.set(instance);
						} catch (Throwable t) {
							createAdaptiveInstanceError = t;
							throw new IllegalStateException(
									"fail to create adaptive instance: "
											+ t.toString(), t);
						}
					}
				}
			} else {
				throw new IllegalStateException(
						"fail to create adaptive instance: "
								+ createAdaptiveInstanceError.toString(),
						createAdaptiveInstanceError);
			}
		}

		return (T) instance;
	}

	private IllegalStateException findException(String name) {
		for (Map.Entry<String, IllegalStateException> entry : exceptions
				.entrySet()) {
			if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
				return entry.getValue();
			}
		}
		StringBuilder buf = new StringBuilder("No such extension "
				+ type.getName() + " by name " + name);

		int i = 1;
		for (Map.Entry<String, IllegalStateException> entry : exceptions
				.entrySet()) {
			if (i == 1) {
				buf.append(", possible causes: ");
			}

			buf.append("\r\n(");
			buf.append(i++);
			buf.append(") ");
			buf.append(entry.getKey());
			buf.append(":\r\n");
			buf.append(StringUtils.toString(entry.getValue()));
		}
		return new IllegalStateException(buf.toString());
	}

	@SuppressWarnings("unchecked")
	private T createExtension(String name) {
		Class<?> clazz = getExtensionClasses().get(name);
		if (clazz == null) {
			throw findException(name);
		}
		try {
			T instance = (T) EXTENSION_INSTANCES.get(clazz);
			if (instance == null) {
				EXTENSION_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
				instance = (T) EXTENSION_INSTANCES.get(clazz);
			}
			injectExtension(instance);
			Set<Class<?>> wrapperClasses = cachedWrapperClasses;
			if (wrapperClasses != null && wrapperClasses.size() > 0) {
				for (Class<?> wrapperClass : wrapperClasses) {
					instance = injectExtension((T) wrapperClass.getConstructor(
							type).newInstance(instance));
				}
			}
			return instance;
		} catch (Throwable t) {
			throw new IllegalStateException("Extension instance(name: " + name
					+ ", class: " + type + ")  could not be instantiated: "
					+ t.getMessage(), t);
		}
	}

	/**
	 * 注入属性
	 * 
	 * 通过instance中的set方法，取得set方法后面的属性，根据该属性找到相应的是实现对象，并自动注入
	 * 
	 * @param instance
	 * @return
	 */
	private T injectExtension(T instance) {
		try {
			if (objectFactory == null) {
				return instance;
			}
			for (Method method : instance.getClass().getMethods()) {
				if (method.getName().startsWith("set")
						&& method.getParameterTypes().length == 1
						&& Modifier.isPublic(method.getModifiers())) {
					Class<?> pt = method.getParameterTypes()[0];
					try {
						String property = method.getName().length() > 3 ? method
								.getName().substring(3, 4).toLowerCase()
								+ method.getName().substring(4)
								: "";
						Object object = objectFactory
								.getExtension(pt, property);
						if (object != null) {
							method.invoke(instance, object);
						}
					} catch (Exception e) {
						logger.error(
								"fail to inject via method " + method.getName()
										+ " of interface " + type.getName()
										+ ": " + e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return instance;
	}

	private Class<?> getExtensionClass(String name) {
		if (type == null)
			throw new IllegalArgumentException("Extension type == null");
		if (name == null)
			throw new IllegalArgumentException("Extension name == null");
		Class<?> clazz = getExtensionClasses().get(name);
		if (clazz == null)
			throw new IllegalStateException("No such extension \"" + name
					+ "\" for " + type.getName() + "!");
		return clazz;
	}

	private Map<String, Class<?>> getExtensionClasses() {
		Map<String, Class<?>> classes = cachedClasses.get();
		if (classes == null) {
			synchronized (cachedClasses) {
				classes = cachedClasses.get();
				if (classes == null) {
					classes = loadExtensionClasses();
					cachedClasses.set(classes);
				}
			}
		}
		return classes;
	}

	// 此方法已经getExtensionClasses方法同步过。
	private Map<String, Class<?>> loadExtensionClasses() {
		final SPI defaultAnnotation = type.getAnnotation(SPI.class);
		if (defaultAnnotation != null) {
			String value = defaultAnnotation.value();
			if (value != null && (value = value.trim()).length() > 0) {
				String[] names = NAME_SEPARATOR.split(value);
				if (names.length > 1) {
					throw new IllegalStateException(
							"more than 1 default extension name on extension "
									+ type.getName() + ": "
									+ Arrays.toString(names));
				}
				if (names.length == 1)
					cachedDefaultName = names[0];
			}
		}

		Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
		loadFiles(extensionClasses, JANUS_INTERNAL_DIRECTORY);
		loadFiles(extensionClasses, JANUS_DIRECTORY);
		loadFiles(extensionClasses, SERVICES_DIRECTORY);
		return extensionClasses;
	}

	private void loadFiles(Map<String, Class<?>> extensionClasses, String dir) {
		String fileName = dir + type.getName();
		try {
			Enumeration<java.net.URL> urls;
			ClassLoader classLoader = findClassLoader();
			if (classLoader != null) {
				urls = classLoader.getResources(fileName);
			} else {
				urls = ClassLoader.getSystemResources(fileName);
			}
			if (urls == null) {
				return;
			}
			while (urls.hasMoreElements()) {
				java.net.URL url = urls.nextElement();
				try {
					loadUrl(extensionClasses, classLoader, url);
				} catch (Throwable t) {
					logger.error(
							"Exception when load extension class(interface: "
									+ type + ", class file: " + url + ") in "
									+ url, t);
				}
			} // end of while urls
		} catch (Throwable t) {
			logger.error("Exception when load extension class(interface: "
					+ type + ", description file: " + fileName + ").", t);
		}
	}

	/**
	 * @param extensionClasses
	 * @param classLoader
	 * @param url
	 */
	private void loadUrl(Map<String, Class<?>> extensionClasses,
			ClassLoader classLoader, java.net.URL url) throws Throwable {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				url.openStream(), "utf-8"));
		try {
			String line = null;
			String className = null;
			while ((line = reader.readLine()) != null) {
				final int ci = line.indexOf('#');
				if (ci >= 0)
					line = line.substring(0, ci);
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				try {
					String name = null;
					int i = line.indexOf('=');
					if (i > 0) {
						name = line.substring(0, i).trim();
						className = line.substring(i + 1).trim();
					}
					if (className.length() == 0) {
						continue;
					}
					Class<?> clazz = Class
							.forName(className, true, classLoader);
					if (!type.isAssignableFrom(clazz)) {
						throwClassTypeError(clazz);
					}
					if (clazz.isAnnotationPresent(Adaptive.class)) {
						if (cachedAdaptiveClass == null) {
							cachedAdaptiveClass = clazz;
						} else if (!cachedAdaptiveClass.equals(clazz)) {
							throwMoreThanOneAdaptive(clazz);
						}
					} else {
						try {
							clazz.getConstructor(type);
							Set<Class<?>> wrappers = cachedWrapperClasses;
							if (wrappers == null) {
								cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
								wrappers = cachedWrapperClasses;
							}
							wrappers.add(clazz);
						} catch (NoSuchMethodException e) {
							parseExtensionClasses(extensionClasses, name, clazz);
						}
					}
				} catch (Throwable t) {
					IllegalStateException e = new IllegalStateException(
							"Failed to load extension class(interface: " + type
									+ ",  className: " + className + ") in "
									+ url + ", cause: " + t.getMessage(), t);
					exceptions.put(className, e);
				}
			} // end of while read lines
		} finally {
			reader.close();
		}

	}

	/**
	 * @param extensionClasses
	 * @param name
	 * @param clazz
	 * @throws NoSuchMethodException
	 */
	private void parseExtensionClasses(Map<String, Class<?>> extensionClasses,
			String name, Class<?> clazz) throws NoSuchMethodException {
		clazz.getConstructor();
		String[] names = NAME_SEPARATOR.split(name);
		if (names == null || names.length == 0) {
			return;
		}
		Activate activate = clazz.getAnnotation(Activate.class);
		if (activate != null) {
			cachedActivates.put(names[0], activate);
		}
		for (String n : names) {
			if (!cachedNames.containsKey(clazz)) {
				cachedNames.put(clazz, n);
			}
			Class<?> c = extensionClasses.get(n);
			if (c == null) {
				extensionClasses.put(n, clazz);
			} else if (c != clazz) {
				throwDuplicateExtensionError(clazz, n, c);
			}
		}
	}

	/**
	 * @param clazz
	 * @param n
	 * @param c
	 */
	private void throwDuplicateExtensionError(Class<?> clazz, String n,
			Class<?> c) {
		throw new IllegalStateException("Duplicate extension " + type.getName()
				+ " name " + n + " on " + c.getName() + " and "
				+ clazz.getName());
	}

	/**
	 * @param clazz
	 */
	private void throwMoreThanOneAdaptive(Class<?> clazz) {
		throw new IllegalStateException("More than 1 adaptive class found: "
				+ cachedAdaptiveClass.getClass().getName() + ", "
				+ clazz.getClass().getName());
	}

	/**
	 * @param clazz
	 */
	private void throwClassTypeError(Class<?> clazz) {
		throw new IllegalStateException(
				"Error when load extension class(interface: " + type
						+ ", class line: " + clazz.getName() + "), class "
						+ clazz.getName() + "is not subtype of interface.");
	}

	@SuppressWarnings("unchecked")
	private T createAdaptiveExtension() {
		try {
			return injectExtension((T) getAdaptiveExtensionClass()
					.newInstance());
		} catch (Exception e) {
			throw new IllegalStateException(
					"Can not create adaptive extenstion " + type + ", cause: "
							+ e.getMessage(), e);
		}
	}

	private Class<?> getAdaptiveExtensionClass() {
		getExtensionClasses();
		if (cachedAdaptiveClass != null) {
			return cachedAdaptiveClass;
		}
		return cachedAdaptiveClass = createAdaptiveExtensionClass();
	}

	private Class<?> createAdaptiveExtensionClass() {
		String code = AdaptiveClassGenerator.createAdaptiveExtensionClassCode(
				type, cachedDefaultName);
		ClassLoader classLoader = findClassLoader();
		com.ctg.itrdc.janus.common.compiler.Compiler compiler = ExtensionLoader
				.getExtensionLoader(
						com.ctg.itrdc.janus.common.compiler.Compiler.class)
				.getAdaptiveExtension();
		return compiler.compile(code, classLoader);
	}

	private static ClassLoader findClassLoader() {
		return ExtensionLoader.class.getClassLoader();
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[" + type.getName() + "]";
	}

}