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
package com.ctg.itrdc.janus.rpc.cluster.directory;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.Router;

import java.util.List;

/**
 * StaticDirectory
 * 
 *
 */
public class StaticDirectory<T> extends AbstractDirectory<T> {
    
    private final List<Invoker<T>> invokers;
    
    public StaticDirectory(List<Invoker<T>> invokers){
        this(null, invokers, null);
    }
    
    public StaticDirectory(List<Invoker<T>> invokers, List<Router> routers){
        this(null, invokers, routers);
    }
    
    public StaticDirectory(URL url, List<Invoker<T>> invokers) {
        this(url, invokers, null);
    }

    public StaticDirectory(URL url, List<Invoker<T>> invokers, List<Router> routers) {
        super(url == null && invokers != null && invokers.size() > 0 ? invokers.get(0).getUrl() : url, routers);
        if (invokers == null || invokers.size() == 0)
            throw new IllegalArgumentException("invokers == null");
        this.invokers = invokers;
    }

    public Class<T> getInterface() {
        return invokers.get(0).getInterface();
    }

    public boolean isAvailable() {
        if (isDestroyed()) {
            return false;
        }
        for (Invoker<T> invoker : invokers) {
            if (invoker.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        if(isDestroyed()) {
            return;
        }
        super.destroy();
        for (Invoker<T> invoker : invokers) {
            invoker.destroy();
        }
        invokers.clear();
    }
    
    @Override
    protected List<Invoker<T>> doList(Invocation invocation) throws RpcException {

        return invokers;
    }

}