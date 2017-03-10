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
package com.ctg.itrdc.janus.rpc.cluster.support;

import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.cluster.Cluster;
import com.ctg.itrdc.janus.rpc.cluster.Directory;

/**
 * 快速失败，只发起一次调用，失败立即报错，通常用于非幂等性的写操作。
 *  
 * <a href="http://en.wikipedia.org/wiki/Fail-fast">Fail-fast</a>
 * 
 * @author william.liangf
 */
public class FailfastCluster implements Cluster {

    public final static String NAME = "failfast";

    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new FailfastClusterInvoker<T>(directory);
    }

}