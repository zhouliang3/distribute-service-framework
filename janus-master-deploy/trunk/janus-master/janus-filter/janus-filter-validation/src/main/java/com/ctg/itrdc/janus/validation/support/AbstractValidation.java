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
package com.ctg.itrdc.janus.validation.support;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.validation.Validation;
import com.ctg.itrdc.janus.validation.Validator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AbstractValidation
 * 
 *
 */
public abstract class AbstractValidation implements Validation {

    private final ConcurrentMap<String, Validator> validators = new ConcurrentHashMap<String, Validator>();

    public Validator getValidator(URL url) {
        String key = url.toFullString();
        Validator validator = validators.get(key);
        if (validator == null) {
            validators.put(key, createValidator(url));
            validator = validators.get(key);
        }
        return validator;
    }

    protected abstract Validator createValidator(URL url);

}
