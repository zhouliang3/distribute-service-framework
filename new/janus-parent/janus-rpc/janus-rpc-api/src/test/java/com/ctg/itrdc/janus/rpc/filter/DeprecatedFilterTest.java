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
package com.ctg.itrdc.janus.rpc.filter;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.LogUtil;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.support.DemoService;
import com.ctg.itrdc.janus.rpc.support.MockInvocation;
import com.ctg.itrdc.janus.rpc.support.MyInvoker;

/**
 * DeprecatedFilterTest.java
 * 
 * @author tony.chenl
 */
public class DeprecatedFilterTest {

    Filter deprecatedFilter = new DeprecatedFilter();

    @Test
    public void testDeprecatedFilter() {
        URL url = URL.valueOf("test://test:11/test?group=janus&version=1.1&echo." + Constants.DEPRECATED_KEY + "=true");
        LogUtil.start();
        deprecatedFilter.invoke(new MyInvoker<DemoService>(url), new MockInvocation());
        assertEquals(1,
                     LogUtil.findMessage("The service method com.ctg.itrdc.janus.rpc.support.DemoService.echo(String) is DEPRECATED"));
        LogUtil.stop();
    }
}