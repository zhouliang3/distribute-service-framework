/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.ctg.itrdc.janus.cache.filter;

import com.ctg.itrdc.janus.cache.Cache;
import com.ctg.itrdc.janus.cache.CacheFactory;
import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.rpc.*;

/**
 * CacheFilter
 *
 *
 */
@Activate(group = {Constants.CONSUMER, Constants.PROVIDER}, value = Constants.CACHE_KEY)
public class CacheFilter implements Filter {

    private CacheFactory cacheFactory;

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (cacheFactory != null && ConfigUtils.isNotEmpty(invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.CACHE_KEY))) {
            Cache cache = cacheFactory.getCache(invoker.getUrl().addParameter(Constants.METHOD_KEY, invocation.getMethodName()));
            if (cache != null) {
                String key = StringUtils.toArgumentString(invocation.getArguments());
                if (cache != null && key != null) {
                    Object value = cache.get(key);
                    if (value != null) {
                        return new RpcResult(value);
                    }
                    Result result = invoker.invoke(invocation);
                    if (!result.hasException()) {
                        cache.put(key, result.getValue());
                    }
                    return result;
                }
            }
        }
        return invoker.invoke(invocation);
    }

}
