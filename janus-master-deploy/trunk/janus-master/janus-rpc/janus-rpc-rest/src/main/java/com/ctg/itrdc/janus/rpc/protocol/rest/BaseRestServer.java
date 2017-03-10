/**
 * Copyright 1999-2014 dangdang.com.
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
package com.ctg.itrdc.janus.rpc.protocol.rest;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import org.jboss.resteasy.spi.ResteasyDeployment;

/**
 *
 */
public abstract class BaseRestServer implements RestServer {

    public void start(URL url) {
        getDeployment().getMediaTypeMappings().put("json", "application/json");
        getDeployment().getMediaTypeMappings().put("xml", "text/xml");
//        server.getDeployment().getMediaTypeMappings().put("xml", "application/xml");
        getDeployment().getProviderClasses().add(RpcContextFilter.class.getName());
        // TODO users can override this mapper, but we just rely on the current priority strategy of resteasy
        getDeployment().getProviderClasses().add(RpcExceptionMapper.class.getName());

        loadProviders(url.getParameter(Constants.EXTENSION_KEY, ""));

        doStart(url);
    }

    public void deploy(Class resourceDef, Object resourceInstance, String contextPath) {
        if (StringUtils.isEmpty(contextPath)) {
            getDeployment().getRegistry().addResourceFactory(new JanusResourceFactory(resourceInstance, resourceDef));
        } else {
            getDeployment().getRegistry().addResourceFactory(new JanusResourceFactory(resourceInstance, resourceDef), contextPath);
        }
    }

    public void undeploy(Class resourceDef) {
        getDeployment().getRegistry().removeRegistrations(resourceDef);
    }

    protected void loadProviders(String value) {
        for (String clazz : Constants.COMMA_SPLIT_PATTERN.split(value)) {
            if (!StringUtils.isEmpty(clazz)) {
                getDeployment().getProviderClasses().add(clazz.trim());
            }
        }
    }

    protected abstract ResteasyDeployment getDeployment();

    protected abstract void doStart(URL url);
}
