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
package com.ctg.itrdc.janus.remoting.exchange.support;

import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeChannel;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeHandler;
import com.ctg.itrdc.janus.remoting.telnet.TelnetHandler;
import com.ctg.itrdc.janus.remoting.telnet.support.TelnetHandlerAdapter;
import com.ctg.itrdc.janus.remoting.transport.ChannelHandlerDispatcher;

/**
 * ExchangeHandlerDispatcher
 * 
 *
 */
public class ExchangeHandlerDispatcher implements ExchangeHandler {

    private final ReplierDispatcher replierDispatcher;

    private final ChannelHandlerDispatcher handlerDispatcher;

    private final TelnetHandler telnetHandler;
    
    public ExchangeHandlerDispatcher() {
        replierDispatcher = new ReplierDispatcher();
        handlerDispatcher = new ChannelHandlerDispatcher();
        telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher(Replier<?> replier){
        replierDispatcher = new ReplierDispatcher(replier);
        handlerDispatcher = new ChannelHandlerDispatcher();
        telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher(ChannelHandler... handlers){
        replierDispatcher = new ReplierDispatcher();
        handlerDispatcher = new ChannelHandlerDispatcher(handlers);
        telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher(Replier<?> replier, ChannelHandler... handlers){
        replierDispatcher = new ReplierDispatcher(replier);
        handlerDispatcher = new ChannelHandlerDispatcher(handlers);
        telnetHandler = new TelnetHandlerAdapter();
    }

    public ExchangeHandlerDispatcher addChannelHandler(ChannelHandler handler) {
        handlerDispatcher.addChannelHandler(handler);
        return this;
    }

    public ExchangeHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
        handlerDispatcher.removeChannelHandler(handler);
        return this;
    }

    public <T> ExchangeHandlerDispatcher addReplier(Class<T> type, Replier<T> replier) {
        replierDispatcher.addReplier(type, replier);
        return this;
    }

    public <T> ExchangeHandlerDispatcher removeReplier(Class<T> type) {
        replierDispatcher.removeReplier(type);
        return this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return ((Replier)replierDispatcher).reply(channel, request);
    }

    public void connected(Channel channel) {
        handlerDispatcher.connected(channel);
    }

    public void disconnected(Channel channel) {
        handlerDispatcher.disconnected(channel);
    }

    public void sent(Channel channel, Object message) {
        handlerDispatcher.sent(channel, message);
    }

    public void received(Channel channel, Object message) {
        handlerDispatcher.received(channel, message);
    }

    public void caught(Channel channel, Throwable exception) {
        handlerDispatcher.caught(channel, exception);
    }

    public String telnet(Channel channel, String message) throws RemotingException {
        return telnetHandler.telnet(channel, message);
    }

}