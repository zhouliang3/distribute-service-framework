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
package com.ctg.itrdc.janus.config.spring;

import java.io.IOException;
import java.net.ServerSocket;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.registry.RegistryService;
import com.ctg.itrdc.janus.rpc.Exporter;
import com.ctg.itrdc.janus.rpc.Protocol;
import com.ctg.itrdc.janus.rpc.ProxyFactory;

/**
 * SimpleRegistryExporter
 * 
 * @author william.liangf
 */
public class SimpleRegistryExporter {
    
    private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();
    
    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
    
    public synchronized static Exporter<RegistryService> exportIfAbsent(int port) {
        try {
            new ServerSocket(port).close();
            return export(port);
        } catch (IOException e) {
            return null;
        }
    }
    
    public static Exporter<RegistryService> export(int port) {
        return export(port, new SimpleRegistryService());
    }
    
    public static Exporter<RegistryService> export(int port, RegistryService registryService) {
        return protocol.export(proxyFactory.getInvoker(registryService, RegistryService.class, 
                new URL("janus", NetUtils.getLocalHost(), port, RegistryService.class.getName())
                .setPath(RegistryService.class.getName())
                .addParameter(Constants.INTERFACE_KEY, RegistryService.class.getName())
                .addParameter(Constants.CLUSTER_STICKY_KEY, "true")
                .addParameter(Constants.CALLBACK_INSTANCES_LIMIT_KEY, "1000")
                .addParameter("ondisconnect", "disconnect")
                .addParameter("subscribe.1.callback", "true")
                .addParameter("unsubscribe.1.callback", "false")));
    }

}