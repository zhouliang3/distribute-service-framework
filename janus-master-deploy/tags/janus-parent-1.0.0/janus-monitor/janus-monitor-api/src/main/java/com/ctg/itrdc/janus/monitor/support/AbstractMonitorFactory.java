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
package com.ctg.itrdc.janus.monitor.support;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.monitor.Monitor;
import com.ctg.itrdc.janus.monitor.MonitorFactory;
import com.ctg.itrdc.janus.monitor.MonitorService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractMonitorFactroy. (SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
public abstract class AbstractMonitorFactory implements MonitorFactory {

    // 注册中心获取过程锁
    private static final ReentrantLock LOCK = new ReentrantLock();
    
    // 注册中心集合 Map<RegistryAddress, Registry>
    private static final Map<String, Monitor> MONITORS = new ConcurrentHashMap<String, Monitor>();

    public static Collection<Monitor> getMonitors() {
        return Collections.unmodifiableCollection(MONITORS.values());
    }

    public Monitor getMonitor(URL url) {
    	url = url.setPath(MonitorService.class.getName()).addParameter(Constants.INTERFACE_KEY, MonitorService.class.getName());
    	String key = url.toServiceString();
        LOCK.lock();
        try {
            Monitor monitor = MONITORS.get(key);
            if (monitor != null) {
                return monitor;
            }
            monitor = createMonitor(url);
            if (monitor == null) {
                throw new IllegalStateException("Can not create monitor " + url);
            }
            MONITORS.put(key, monitor);
            return monitor;
        } finally {
            // 释放锁
            LOCK.unlock();
        }
    }

    protected abstract Monitor createMonitor(URL url);

}