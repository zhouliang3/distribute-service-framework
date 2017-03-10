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
package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 配置工厂接口
 * 
 * 负责重写url的参数。重写有多种方式，比如直接覆盖，或者如果不存在，就增加等等方式。
 * 
 * @author Administrator
 */
@SPI
public interface ConfiguratorFactory {

	/**
	 * get the configurator instance.
	 * 
	 * @param url
	 *            - configurator url.
	 * @return configurator instance.
	 */
	@Adaptive("protocol")
	Configurator getConfigurator(URL url);

}
