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
package com.ctg.itrdc.janus.rpc.protocol.janus.filter;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.json.JSON;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.ConcurrentHashSet;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcContext;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * TraceFilter
 * 
 * @author william.liangf
 */
@Activate(group = Constants.PROVIDER)
public class TraceFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);
    
    private static final String TRACE_MAX = "trace.max";
    
    private static final String TRACE_COUNT = "trace.count";
    
    private static final ConcurrentMap<String, Set<Channel>> tracers = new ConcurrentHashMap<String, Set<Channel>>();
    
    public static void addTracer(Class<?> type, String method, Channel channel, int max) {
        channel.setAttribute(TRACE_MAX, max);
        channel.setAttribute(TRACE_COUNT, new AtomicInteger());
        String key = method != null && method.length() > 0 ? type.getName() + "." + method : type.getName();
        Set<Channel> channels = tracers.get(key);
        if (channels == null) {
            tracers.putIfAbsent(key, new ConcurrentHashSet<Channel>());
            channels = tracers.get(key);
        }
        channels.add(channel);
    }
    
    public static void removeTracer(Class<?> type, String method, Channel channel) {
        channel.removeAttribute(TRACE_MAX);
        channel.removeAttribute(TRACE_COUNT);
        String key = method != null && method.length() > 0 ? type.getName() + "." + method : type.getName();
        Set<Channel> channels = tracers.get(key);
        if (channels != null) {
            channels.remove(channel);
        }
    }
    
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = System.currentTimeMillis();
        Result result = invoker.invoke(invocation);
        long end = System.currentTimeMillis();
        if (tracers.size() > 0) {
            String key = invoker.getInterface().getName() + "." + invocation.getMethodName();
            Set<Channel> channels = tracers.get(key);
            if (channels == null || channels.size() == 0) {
                key = invoker.getInterface().getName();
                channels = tracers.get(key);
            }
            if (channels != null && channels.size() > 0) {
                for (Channel channel : new ArrayList<Channel>(channels)) {
                    if (channel.isConnected()) {
                        try {
                            int max = 1;
                            Integer m = (Integer) channel.getAttribute(TRACE_MAX);
                            if (m != null) {
                                max = (int) m;
                            }
                            int count = 0;
                            AtomicInteger c = (AtomicInteger) channel.getAttribute(TRACE_COUNT);
                            if (c == null) {
                                c = new AtomicInteger();
                                channel.setAttribute(TRACE_COUNT, c);
                            }
                            count = c.getAndIncrement();
                            if (count < max) {
                                String prompt = channel.getUrl().getParameter(Constants.PROMPT_KEY, Constants.DEFAULT_PROMPT);
                                channel.send("\r\n" + RpcContext.getContext().getRemoteAddress() + " -> "  
                                         + invoker.getInterface().getName() 
                                         + "." + invocation.getMethodName() 
                                         + "(" + JSON.json(invocation.getArguments()) + ")" + " -> " + JSON.json(result.getValue())
                                         + "\r\nelapsed: "+(end - start) +" ms."
                                         + "\r\n\r\n" + prompt);
                            }
                            if(count >= max - 1) {
                                channels.remove(channel);
                            }
                        } catch (Throwable e) {
                            channels.remove(channel);
                            logger.warn(e.getMessage(), e);
                        }
                    } else {
                        channels.remove(channel);
                    }
                }
            }
        }
        return result;
    }

}