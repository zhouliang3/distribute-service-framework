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
package com.ctg.itrdc.janus.rpc.cluster.configurator.absent;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.cluster.configurator.AbstractConfigurator;

/**
 * 配置器absent实现
 * 
 *
 */
public class AbsentConfigurator extends AbstractConfigurator {

	public AbsentConfigurator(URL url) {
		super(url);
	}

	/**
	 * 进行配置的合并。
	 * 
	 * currentUrl是本地需要被覆盖参数的url；configUrl是要覆盖的目的url。<br/>
	 * 合并处理后，假如currentUrl中有存在参数的话，不做处理，直接忽略。否则将configUrl参数，合并到currentUrl中。
	 */
	public URL doConfigure(URL currentUrl, URL configUrl) {
		return currentUrl.addParametersIfAbsent(configUrl.getParameters());
	}

}
