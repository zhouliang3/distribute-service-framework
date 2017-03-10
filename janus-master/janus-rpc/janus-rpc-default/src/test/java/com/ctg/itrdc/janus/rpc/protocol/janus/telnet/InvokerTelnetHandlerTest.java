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
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.NetUtils;
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
public class InvokerTelnetHandlerTest {

    private static TelnetHandler invoke = new InvokeTelnetHandler();
    private Channel              mockChannel;
    private Invoker<DemoService> mockInvoker;

    @After
    public void after() {
       ProtocolUtils.closeAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInvokeDefaultSService() throws RemotingException {
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:20883/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn("com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService").anyTimes();
        EasyMock.expect(mockChannel.getLocalAddress()).andReturn(NetUtils.toAddress("127.0.0.1:5555")).anyTimes();
        EasyMock.expect(mockChannel.getRemoteAddress()).andReturn(NetUtils.toAddress("127.0.0.1:20883")).anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getJanusProtocol().export(mockInvoker);
        String result = invoke.telnet(mockChannel, "DemoService.echo(\"ok\")");
        assertTrue(result.contains("Use default service com.ctg.itrdc.janus.rpc.protocol.janus.support.DemoService.\r\n\"ok\"\r\n"));
        EasyMock.reset(mockChannel, mockInvoker);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInvokeAutoFindMethod() throws RemotingException {
        mockInvoker = EasyMock.createMock(Invoker.class);
        EasyMock.expect(mockInvoker.getInterface()).andReturn(DemoService.class).anyTimes();
        EasyMock.expect(mockInvoker.getUrl()).andReturn(URL.valueOf("janus://127.0.0.1:20883/demo")).anyTimes();
        EasyMock.expect(mockInvoker.invoke((Invocation) EasyMock.anyObject())).andReturn(new RpcResult("ok")).anyTimes();
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn(null).anyTimes();
        EasyMock.expect(mockChannel.getLocalAddress()).andReturn(NetUtils.toAddress("127.0.0.1:5555")).anyTimes();
        EasyMock.expect(mockChannel.getRemoteAddress()).andReturn(NetUtils.toAddress("127.0.0.1:20883")).anyTimes();
        EasyMock.replay(mockChannel, mockInvoker);
        JanusProtocol.getJanusProtocol().export(mockInvoker);
        String result = invoke.telnet(mockChannel, "echo(\"ok\")");
        assertTrue(result.contains("ok"));
        EasyMock.reset(mockChannel, mockInvoker);
    }

    @Test
    public void testMessageNull() throws RemotingException {
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn(null).anyTimes();
        EasyMock.replay(mockChannel);
        String result = invoke.telnet(mockChannel, null);
        assertEquals("Please input method name, eg: \r\ninvoke xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})\r\ninvoke XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})\r\ninvoke com.xxx.XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})",
                     result);
        EasyMock.reset(mockChannel);
    }

    @Test
    public void testInvaildMessage() throws RemotingException {
        mockChannel = EasyMock.createMock(Channel.class);
        EasyMock.expect(mockChannel.getAttribute("telnet.service")).andReturn(null).anyTimes();
        EasyMock.replay(mockChannel);
        String result = invoke.telnet(mockChannel, "(");
        assertEquals("Invalid parameters, format: service.method(args)", result);
        EasyMock.reset(mockChannel);
    }
}