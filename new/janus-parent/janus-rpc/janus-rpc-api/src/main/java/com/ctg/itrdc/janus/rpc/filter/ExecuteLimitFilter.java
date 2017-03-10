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
package com.ctg.itrdc.janus.rpc.filter;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.RpcStatus;

/**
 * 线程数限制过滤器。 如果当前的线程数大于设置的executes数，抛出异常，不能调用
 * 
 * @author william.liangf
 */
@Activate(group = Constants.PROVIDER, value = Constants.EXECUTES_KEY)
public class ExecuteLimitFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		URL url = invoker.getUrl();
		String methodName = invocation.getMethodName();
		int max = url.getMethodParameter(methodName, Constants.EXECUTES_KEY, 0);
		if (max > 0) {
			RpcStatus count = RpcStatus.getStatus(url,
					invocation.getMethodName());
			if (count.getActive() >= max) {
				throw new RpcException(
						"Failed to invoke method "
								+ invocation.getMethodName()
								+ " in provider "
								+ url
								+ ", cause: The service using threads greater than <janus:service executes=\""
								+ max + "\" /> limited.");
			}
		}
		long begin = System.currentTimeMillis();
		boolean isException = false;
		RpcStatus.beginCount(url, methodName);
		try {
			Result result = invoker.invoke(invocation);
			return result;
		} catch (Throwable t) {
			isException = true;
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new RpcException(
						"unexpected exception when ExecuteLimitFilter", t);
			}
		} finally {
			RpcStatus.endCount(url, methodName, System.currentTimeMillis()
					- begin, isException);
		}
	}

}