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
package com.ctg.itrdc.janus.rpc.protocol.janus.telnet;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.common.utils.ReflectUtils;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.RemotingException;
import com.ctg.itrdc.janus.remoting.telnet.TelnetHandler;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcResult;
import com.ctg.itrdc.janus.rpc.protocol.janus.JanusProtocol;
import com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService;
import com.ctg.itrdc.janus.rpc.protocol.janus.support.ProtocolUtils;

/**
 * CountTelnetHandlerTest.java
 * 
 * @author tony.chenl
 */
public class ListTelnetHandlerTest {

    private static TelnetHandler list = new ListTelnetHandler();
    private Channel              mockChannel;
    private Invoker<DemoService> mockInvoker;
    private static String        detailMethods;
    private static String        methodsName;

    @BeforeClass
    public static void setUp() {
        StringBuilder buf = new StringBuilder();
        StringBuilder buf2 = new StringBuilder();
        Method[] methods = DemoService.class.getMethods();
        for (Method method : methods) {
            if (buf.length() > 0) {
                buf.append("\r\n");
            }
            if (buf2.length() > 0) {
                buf2.append("\r\n");
            }
            buf2.append(method.getName());
            buf.append(ReflectUtils.getName(method));
        }
        detailMethods = buf.toString();
        methodsName = buf2.toString();
        
        ProtocolUtils.closeAll();
    }
    
    @After
    public void after() {
        ProtocolUtils.closeAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListDetailService() throws RemotingException {
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:20885/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn("com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService").anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getDubboProtocol().export(mockInvoker);
        String result = list.telnet(mockChannel, "-l DemoService");
        assertEquals(detailMethods, result);
        EasyMock.reset(mockChannel, mockInvoker);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListService() throws RemotingException {
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:20885/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn("com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService").anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getDubboProtocol().export(mockInvoker);
        String result = list.telnet(mockChannel, "DemoService");
        assertEquals(methodsName, result);
        EasyMock.reset(mockChannel, mockInvoker);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testList() throws RemotingException {
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:20885/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn(null).anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getDubboProtocol().export(mockInvoker);
        String result = list.telnet(mockChannel, "");
        assertEquals("com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService", result);
        EasyMock.reset(mockChannel);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListDetail() throws RemotingException {
        int port = NetUtils.getAvailablePort();
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:"+port+"/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn(null).anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getDubboProtocol().export(mockInvoker);
        String result = list.telnet(mockChannel, "-l");
        assertEquals("com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService -> janus://127.0.0.1:"+port+"/demo", result);
        EasyMock.reset(mockChannel);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListDefault() throws RemotingException {
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:20885/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn("com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService").anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getDubboProtocol().export(mockInvoker);
        String result = list.telnet(mockChannel, "");
        assertEquals("Use default service com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService.\r\n\r\n"
                     + methodsName, result);
        EasyMock.reset(mockChannel);
    }

    @Test
    public void testInvaildMessage() throws RemotingException {
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn(null).anyTimes();
        EasyMock.replay(mockChannel);
        String result = list.telnet(mockChannel, "xx");
        assertEquals("No such service xx", result);
        EasyMock.reset(mockChannel);
    }
}