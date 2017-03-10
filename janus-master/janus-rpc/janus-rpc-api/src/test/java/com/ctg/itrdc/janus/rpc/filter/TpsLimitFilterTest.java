/*
 * Copyright 1999-2012 Alibaba Group.
 *    
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *    
 *        http://www.apache.org/licenses/LICENSE-2.0
 *    
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.ctg.itrdc.janus.rpc.filter;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.RpcStatus;
import com.ctg.itrdc.janus.rpc.support.MockInvocation;
import com.ctg.itrdc.janus.rpc.support.MyInvoker;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
public class TpsLimitFilterTest {

    private TpsLimitFilter filter = new TpsLimitFilter();

    @Test
    public void testWithoutCount() throws Exception {
        URL url = URL.valueOf("test://test");
        url = url.addParameter(Constants.INTERFACE_KEY,
                               "com.ctg.itrdc.janus.rpc.file.TpsService");
        url = url.addParameter(Constants.TPS_LIMIT_RATE_KEY, 5);
        Invoker<TpsLimitFilterTest> invoker = new MyInvoker<TpsLimitFilterTest>(url);
        Invocation invocation = new MockInvocation();
        filter.invoke(invoker, invocation);
    }
    
    @Test(expected = RpcException.class)
    public void testFail() throws Exception {
        URL url = URL.valueOf("test://test");
        url = url.addParameter(Constants.INTERFACE_KEY,
                               "com.ctg.itrdc.janus.rpc.file.TpsService");
        url = url.addParameter(Constants.TPS_LIMIT_RATE_KEY, 5);
        Invoker<TpsLimitFilterTest> invoker = new MyInvoker<TpsLimitFilterTest>(url);
        Invocation invocation = new MockInvocation();
        for (int i = 0; i < 10; i++) {
            try {
                filter.invoke(invoker, invocation);
            } catch (Exception e) {
                assertTrue(i >= 5);
                throw e;
            }
        }
    }

}
