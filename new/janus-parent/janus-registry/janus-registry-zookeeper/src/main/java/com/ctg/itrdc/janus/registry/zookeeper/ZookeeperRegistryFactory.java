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
package com.ctg.itrdc.janus.registry.zookeeper;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.registry.Registry;
import com.ctg.itrdc.janus.registry.support.AbstractRegistryFactory;
import com.ctg.itrdc.janus.remoting.zookeeper.ZookeeperTransporter;

/**
 * ZookeeperRegistryFactory.
 * 
 * @author william.liangf
 */
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {
	
	private ZookeeperTransporter zookeeperTransporter;

    public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
		this.zookeeperTransporter = zookeeperTransporter;
	}

	public Registry createRegistry(URL url) {
        return new ZookeeperRegistry(url, zookeeperTransporter);
    }

}