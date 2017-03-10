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
package com.ctg.itrdc.janus.validation.filter;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.rpc.*;
import com.ctg.itrdc.janus.validation.Validation;
import com.ctg.itrdc.janus.validation.Validator;

/**
 * ValidationFilter
 * 
 *
 */
@Activate(group = { Constants.CONSUMER, Constants.PROVIDER }, value = Constants.VALIDATION_KEY, order = 10000)
public class ValidationFilter implements Filter {

    private Validation validation;

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (validation != null && ! invocation.getMethodName().startsWith("$") 
                && ConfigUtils.isNotEmpty(invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.VALIDATION_KEY))) {
            try {
                Validator validator = validation.getValidator(invoker.getUrl());
                if (validator != null) {
                    validator.validate(invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments());
                }
            } catch (RpcException e) {
                throw e;
            } catch (Throwable t) {
                throw new RpcException(t.getMessage(), t);
            }
        }
        return invoker.invoke(invocation);
    }

}