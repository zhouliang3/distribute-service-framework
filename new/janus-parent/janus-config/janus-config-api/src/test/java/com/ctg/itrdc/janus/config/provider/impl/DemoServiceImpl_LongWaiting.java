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
package com.ctg.itrdc.janus.config.provider.impl;

import java.util.List;

import com.ctg.itrdc.janus.config.api.Box;
import com.ctg.itrdc.janus.config.api.DemoException;
import com.ctg.itrdc.janus.config.api.DemoService;
import com.ctg.itrdc.janus.config.api.User;

/**
 * DemoServiceImpl
 * 
 * @author william.liangf
 */
public class DemoServiceImpl_LongWaiting implements DemoService {
    
    public String sayName(String name) {
        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {}
        
        return "say:" + name;
    }
    
    public Box getBox() {
        return null;
    }

    public void throwDemoException() throws DemoException {
        throw new DemoException("LongWaiting");
    }

    public List<User> getUsers(List<User> users) {
        return users;
    }

    public int echo(int i) {
        return i;
    }
    
}