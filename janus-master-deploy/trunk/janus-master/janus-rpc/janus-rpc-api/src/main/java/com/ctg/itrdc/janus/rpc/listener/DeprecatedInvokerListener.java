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
package com.ctg.itrdc.janus.rpc.listener;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;

/**
 * DeprecatedProtocolFilter
 * 
 *
 */
@Activate(Constants.DEPRECATED_KEY)
public class DeprecatedInvokerListener extends InvokerListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedInvokerListener.class);

    public void referred(Invoker<?> invoker) throws RpcException {
        if (invoker.getUrl().getParameter(Constants.DEPRECATED_KEY, false)) {
            LOGGER.error("The service " + invoker.getInterface().getName() + " is DEPRECATED! Declare from " + invoker.getUrl());
        }
    }

}