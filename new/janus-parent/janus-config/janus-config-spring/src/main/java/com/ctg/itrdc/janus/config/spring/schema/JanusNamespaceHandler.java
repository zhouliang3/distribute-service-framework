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
package com.ctg.itrdc.janus.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.ctg.itrdc.janus.common.Version;
import com.ctg.itrdc.janus.config.ApplicationConfig;
import com.ctg.itrdc.janus.config.ConsumerConfig;
import com.ctg.itrdc.janus.config.ModuleConfig;
import com.ctg.itrdc.janus.config.MonitorConfig;
import com.ctg.itrdc.janus.config.ProtocolConfig;
import com.ctg.itrdc.janus.config.ProviderConfig;
import com.ctg.itrdc.janus.config.RegistryConfig;
import com.ctg.itrdc.janus.config.spring.AnnotationBean;
import com.ctg.itrdc.janus.config.spring.ReferenceBean;
import com.ctg.itrdc.janus.config.spring.ServiceBean;

/**
 * JanusNamespaceHandler
 * 
 * @author william.liangf
 * @export
 */
public class JanusNamespaceHandler extends NamespaceHandlerSupport {

	static {
		Version.checkDuplicate(JanusNamespaceHandler.class);
	}

	public void init() {
	    registerBeanDefinitionParser("application", new JanusBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("module", new JanusBeanDefinitionParser(ModuleConfig.class, true));
        registerBeanDefinitionParser("registry", new JanusBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("monitor", new JanusBeanDefinitionParser(MonitorConfig.class, true));
        registerBeanDefinitionParser("provider", new JanusBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new JanusBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new JanusBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new JanusBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("reference", new JanusBeanDefinitionParser(ReferenceBean.class, false));
        registerBeanDefinitionParser("annotation", new JanusBeanDefinitionParser(AnnotationBean.class, true));
    }

}