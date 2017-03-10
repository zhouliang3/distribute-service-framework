/*
 * Copyright 2016-2017 CHINA TELECOM GROUP.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ctg.itrdc.janus.common.bytecode;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ctg.itrdc.janus.common.utils.ClassHelper;
import com.ctg.itrdc.janus.common.utils.ReflectUtils;

/**
 * Proxy.
 * 
 *
 */

public abstract class Proxy {
	private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);

	private static final String PACKAGE_NAME = Proxy.class.getPackage()
			.getName();

	public static final InvocationHandler RETURN_NULL_INVOKER = new InvocationHandler() {
		public Object invoke(Object proxy, Method method, Object[] args) {
			return null;
		}
	};

	public static final InvocationHandler THROW_UNSUPPORTED_INVOKER = new InvocationHandler() {
		public Object invoke(Object proxy, Method method, Object[] args) {
			throw new UnsupportedOperationException("Method ["
					+ ReflectUtils.getName(method) + "] unimplemented.");
		}
	};

	private static final Map<ClassLoader, Map<String, Object>> ProxyCacheMap = new WeakHashMap<ClassLoader, Map<String, Object>>();

	private static final Object PendingGenerationMarker = new Object();

	/**
	 * Get proxy.
	 * 
	 * @param ics
	 *            interface class array.
	 * @return Proxy instance.
	 */
	public static Proxy getProxy(Class<?>... ics) {
		return getProxy(ClassHelper.getCallerClassLoader(Proxy.class), ics);
	}

	/**
	 * Get proxy.
	 * 
	 * @param cl
	 *            class loader.
	 * @param ics
	 *            interface class array.
	 * 
	 * @return Proxy instance.
	 */
	public static Proxy getProxy(ClassLoader cl, Class<?>... ics) {
		if (ics.length > 65535)
			throw new IllegalArgumentException("interface limit exceeded");

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ics.length; i++) {//inote 1. 遍历所有入参接口，以；分割连接起来， 以它为key以map为缓存查找如果有，说明代理对象已创建返回
			String itf = ics[i].getName();
			if (!ics[i].isInterface())
				throw new RuntimeException(itf + " is not a interface.");

			Class<?> tmp = null;
			try {
				tmp = Class.forName(itf, false, cl);
			} catch (ClassNotFoundException e) {
			}

			if (tmp != ics[i])
				throw new IllegalArgumentException(ics[i]
						+ " is not visible from class loader");

			sb.append(itf).append(';');
		}

		// use interface class name list as key.
		String key = sb.toString();

		// get cache by class loader.
		Map<String, Object> cache;
		synchronized (ProxyCacheMap) {
			cache = ProxyCacheMap.get(cl);
			if (cache == null) {
				cache = new HashMap<String, Object>();
				ProxyCacheMap.put(cl, cache);
			}
		}

		Proxy proxy = null;
		synchronized (cache) {
			do {
				Object value = cache.get(key);
				if (value instanceof Reference<?>) {
					proxy = (Proxy) ((Reference<?>) value).get();//idoubt 为何此处要用一个弱引用来存放instance对象
					if (proxy != null)
						return proxy;
				}

				if (value == PendingGenerationMarker) {
					try {
						cache.wait();//idoubt 此处线程在等待，何时唤醒，表明有线程正在执行同样的生成代理对象
					} catch (InterruptedException e) {
					}
				} else {
					cache.put(key, PendingGenerationMarker);//inote PendingGenerationMarker在此处放入的，有啥用？？？
					break;
				}
			} while (true);
		}

		long id = PROXY_CLASS_COUNTER.getAndIncrement();//inote 2.利用AtomicLong对象自增获取一个long数组来作为生产类的后缀，防止冲突
		String pkg = null;
		ClassGenerator ccp = null, ccm = null;
		try {
			ccp = ClassGenerator.newInstance(cl);

			Set<String> worked = new HashSet<String>();
			List<Method> methods = new ArrayList<Method>();

			for (int i = 0; i < ics.length; i++) {
				if (!Modifier.isPublic(ics[i].getModifiers())) {
					String npkg = ics[i].getPackage().getName();
					if (pkg == null) {
						pkg = npkg;
					} else {
						if (!pkg.equals(npkg))
							throw new IllegalArgumentException(
									"non-public interfaces from different packages");
					}
				}
				ccp.addInterface(ics[i]);

				for (Method method : ics[i].getMethods()) {
					String desc = ReflectUtils.getDesc(method);
					if (worked.contains(desc))
						continue;
					worked.add(desc);

					int ix = methods.size();
					Class<?> rt = method.getReturnType();
					Class<?>[] pts = method.getParameterTypes();

					StringBuilder code = new StringBuilder(
							"Object[] args = new Object[").append(pts.length)
							.append("];");
					for (int j = 0; j < pts.length; j++)
						code.append(" args[").append(j).append("] = ($w)$")
								.append(j + 1).append(";");
					code.append(" Object ret = handler.invoke(this, methods["
							+ ix + "], args);");//inote 构建方法体，这里的方法调用其实是委托给InvokerInvocationHandler实例对象的，去调用真正的实例
					if (!Void.TYPE.equals(rt))
						code.append(" return ").append(asArgument(rt, "ret"))
								.append(";");

					methods.add(method);
					ccp.addMethod(method.getName(), method.getModifiers(), rt,
							pts, method.getExceptionTypes(), code.toString());
				}
			}

			if (pkg == null)
				pkg = PACKAGE_NAME;

			// create ProxyInstance class.
			String pcn = pkg + ".proxy" + id;
			ccp.setClassName(pcn);
			ccp.addField("public static java.lang.reflect.Method[] methods;");
			ccp.addField("private " + InvocationHandler.class.getName()
					+ " handler;");
			ccp.addConstructor(Modifier.PUBLIC,
					new Class<?>[] { InvocationHandler.class },
					new Class<?>[0], "handler=$1;");
			ccp.addDefaultConstructor();
			Class<?> clazz = ccp.toClass();
			clazz.getField("methods").set(null, methods.toArray(new Method[0]));

			// create Proxy class.
			String fcn = Proxy.class.getName() + id;
			ccm = ClassGenerator.newInstance(cl);
			ccm.setClassName(fcn);
			ccm.addDefaultConstructor();
			ccm.setSuperClass(Proxy.class);
			ccm.addMethod("public Object newInstance("
					+ InvocationHandler.class.getName() + " h){ return new "
					+ pcn + "($1); }");
			Class<?> pc = ccm.toClass();
			proxy = (Proxy) pc.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			// release ClassGenerator
			if (ccp != null)
				ccp.release();
			if (ccm != null)
				ccm.release();
			synchronized (cache) {
				if (proxy == null)
					cache.remove(key);
				else
					cache.put(key, new WeakReference<Proxy>(proxy));//inote WeakReference弱引用
				cache.notifyAll();//inote 唤醒在等待cache的线程
			}
		}
		return proxy;
	}

	/**
	 * get instance with default handler.
	 * 
	 * @return instance.
	 */
	public Object newInstance() {
		return newInstance(THROW_UNSUPPORTED_INVOKER);
	}

	/**
	 * get instance with special handler.
	 * 
	 * @return instance.
	 */
	abstract public Object newInstance(InvocationHandler handler);

	protected Proxy() {
	}

	/**
	 * 获取属性的返回表达式的字符串表示
	 * 
	 * @param cl
	 * @param name
	 * @return
	 */
	private static String asArgument(Class<?> cl, String name) {
		if (cl.isPrimitive()) {
			if (Boolean.TYPE == cl)
				return name + "==null?false:((Boolean)" + name
						+ ").booleanValue()";
			if (Byte.TYPE == cl)
				return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
			if (Character.TYPE == cl)
				return name + "==null?(char)0:((Character)" + name
						+ ").charValue()";
			if (Double.TYPE == cl)
				return name + "==null?(double)0:((Double)" + name
						+ ").doubleValue()";
			if (Float.TYPE == cl)
				return name + "==null?(float)0:((Float)" + name
						+ ").floatValue()";
			if (Integer.TYPE == cl)
				return name + "==null?(int)0:((Integer)" + name
						+ ").intValue()";
			if (Long.TYPE == cl)
				return name + "==null?(long)0:((Long)" + name + ").longValue()";
			if (Short.TYPE == cl)
				return name + "==null?(short)0:((Short)" + name
						+ ").shortValue()";
			throw new RuntimeException(name + " is unknown primitive type.");
		}
		return "(" + ReflectUtils.getName(cl) + ")" + name;
	}
}