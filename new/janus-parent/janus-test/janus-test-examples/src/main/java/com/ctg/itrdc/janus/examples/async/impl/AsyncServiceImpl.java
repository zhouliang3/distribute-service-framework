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
package com.ctg.itrdc.janus.examples.async.impl;

import com.ctg.itrdc.janus.examples.async.api.AsyncService;

/**
 * AsyncServiceImpl
 * 
 * @author william.liangf
 */
public class AsyncServiceImpl implements AsyncService {

    public String sayHello(String name) {
        System.out.println("async provider received: " + name);
        return "hello, " + name;
    }

}