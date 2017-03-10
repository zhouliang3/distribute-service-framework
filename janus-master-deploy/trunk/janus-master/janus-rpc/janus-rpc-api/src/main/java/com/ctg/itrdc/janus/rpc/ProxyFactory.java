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
package com.ctg.itrdc.janus.rpc;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 代理工厂类. (API/SPI, Singleton, ThreadSafe)
 * 
 * @author Administrator
 */
@SPI("javassist")
public interface ProxyFactory {

	/**
	 * 根据Invoker对象，创建该invoker的代理对象
	 * 
	 * @param invoker
	 * @return proxy
	 */
	@Adaptive({ Constants.PROXY_KEY })
	<T> T getProxy(Invoker<T> invoker) throws RpcException;

	/**
	 * 根据代理对象，获取对该对象调用的invoker对象
	 * 
	 * @param <T>
	 * @param proxy
	 * @param type
	 * @param url
	 * @return invoker
	 */
	@Adaptive({ Constants.PROXY_KEY })
	<T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url)
			throws RpcException;

}