/*
 * Copyright 1999-2011 Alibaba Group.
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
package com.ctg.itrdc.janus.rpc.proxy.javassist;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.bytecode.Proxy;
import com.ctg.itrdc.janus.common.bytecode.Wrapper;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.proxy.AbstractProxyFactory;
import com.ctg.itrdc.janus.rpc.proxy.AbstractProxyInvoker;
import com.ctg.itrdc.janus.rpc.proxy.InvokerInvocationHandler;

/**
 * Javaassist Rpc 代理工厂
 * 
 * @author Administrator
 */
public class JavassistProxyFactory extends AbstractProxyFactory {

	/**
	 * 根据invoker对象，获取代理对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
		return (T) Proxy.getProxy(interfaces).newInstance(
				new InvokerInvocationHandler(invoker));
	}

	/**
	 * 根据代理对象，获取Invoker
	 */
	public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
		// TODO Wrapper类不能正确处理带$的类名
		final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName()
				.indexOf('$') < 0 ? proxy.getClass() : type);
		return new AbstractProxyInvoker<T>(proxy, type, url) {
			@Override
			protected Object doInvoke(T proxy, String methodName,
					Class<?>[] parameterTypes, Object[] arguments)
					throws Throwable {
				return wrapper.invokeMethod(proxy, methodName, parameterTypes,
						arguments);
			}
		};
	}

}