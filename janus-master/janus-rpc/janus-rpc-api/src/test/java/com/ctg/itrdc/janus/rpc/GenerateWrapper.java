package com.ctg.itrdc.janus.rpc;

import com.ctg.itrdc.janus.common.bytecode.Wrapper;

public class GenerateWrapper extends Wrapper {

	public static String[] pns;
	public static java.util.Map pts;
	public static String[] mns;
	public static String[] dmns;
	public static Class[] mts0;
	public static Class[] mts1;
	public static Class[] mts2;
	public static Class[] mts3;
	public static Class[] mts4;
	public static Class[] mts5;
	public static Class[] mts6;
	public static Class[] mts7;
	public static Class[] mts8;
	public static Class[] mts9;

	public String[] getPropertyNames() {
		return pns;
	}

	public boolean hasProperty(String n) {
		return pts.containsKey(n);
	}

	public Class getPropertyType(String n) {
		return (Class) pts.get(n);
	}

	public String[] getMethodNames() {
		return mns;
	}

	public String[] getDeclaredMethodNames() {
		return dmns;
	}

	public void setPropertyValue(Object o, String n, Object v) {
		com.ctg.itrdc.janus.rpc.DemoServiceImpl w;
		try {
			w = ((com.ctg.itrdc.janus.rpc.DemoServiceImpl) o);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
		throw new com.ctg.itrdc.janus.common.bytecode.NoSuchPropertyException(
				"Not found property \""
						+ n
						+ "\" filed or setter method in class com.ctg.itrdc.janus.rpc.DemoServiceImpl.");
	}

	public Object getPropertyValue(Object o, String n) {
		com.ctg.itrdc.janus.rpc.DemoServiceImpl w;
		try {
			w = ((com.ctg.itrdc.janus.rpc.DemoServiceImpl) o);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
		if (n.equals("threadName")) {
			return w.getThreadName();
		}
		throw new com.ctg.itrdc.janus.common.bytecode.NoSuchPropertyException(
				"Not found property \""
						+ n
						+ "\" filed or setter method in class com.ctg.itrdc.janus.rpc.DemoServiceImpl.");
	}

	public Object invokeMethod(Object o, String n, Class[] p, Object[] v)
			throws java.lang.reflect.InvocationTargetException {
		com.ctg.itrdc.janus.rpc.DemoServiceImpl w;
		try {
			w = ((com.ctg.itrdc.janus.rpc.DemoServiceImpl) o);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
		try {
			if ("invoke".equals(n) && p.length == 2) {
				return w.invoke((java.lang.String) v[0],
						(java.lang.String) v[1]);
			}
			if ("timestamp".equals(n) && p.length == 0) {
				return w.timestamp();
			}
			if ("getSize".equals(n) && p.length == 1
					&& p[0].getName().equals("[Ljava.lang.String;")) {
				return w.getSize((java.lang.String[]) v[0]);
			}
			if ("getSize".equals(v) && p.length == 1
					&& p[0].getName().equals("[Ljava.lang.Object;")) {
				return w.getSize((java.lang.Object[]) v[0]);
			}
			if ("echo".equals(n) && p.length == 1) {
				return w.echo((java.lang.String) v[0]);
			}
			if ("sayHello".equals(n) && p.length == 1) {
				w.sayHello((java.lang.String) v[0]);
				return null;
			}
			if ("getThreadName".equals(n) && p.length == 0) {
				return w.getThreadName();
			}
			if ("throwTimeout".equals(n) && p.length == 0) {
				w.throwTimeout();
				return null;
			}
			if ("stringLength".equals(n) && p.length == 1) {
				return w.stringLength((java.lang.String) v[0]);
			}
			if ("enumlength".equals(n) && p.length == 1) {
				return w.enumlength((com.ctg.itrdc.janus.rpc.Type[]) v[0]);
			}
		} catch (Throwable e) {
			throw new java.lang.reflect.InvocationTargetException(e);
		}
		throw new com.ctg.itrdc.janus.common.bytecode.NoSuchMethodException(
				"Not found method \"" + n
						+ "\" in class com.ctg.itrdc.janus.rpc.DemoServiceImpl.");
	}

}