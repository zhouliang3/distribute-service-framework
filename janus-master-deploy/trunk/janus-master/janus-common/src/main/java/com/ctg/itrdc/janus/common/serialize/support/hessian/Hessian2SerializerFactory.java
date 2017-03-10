package com.ctg.itrdc.janus.common.serialize.support.hessian;

import com.alibaba.com.caucho.hessian.io.SerializerFactory;

/**
 * Hessian2序列化工厂
 * 
 * @author Administrator
 */
public class Hessian2SerializerFactory extends SerializerFactory {

	public static final SerializerFactory SERIALIZER_FACTORY = new Hessian2SerializerFactory();

	private Hessian2SerializerFactory() {
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

}
