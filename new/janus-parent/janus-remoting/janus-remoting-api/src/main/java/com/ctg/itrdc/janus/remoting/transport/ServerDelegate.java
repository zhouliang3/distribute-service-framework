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
package com.ctg.itrdc.janus.remoting.transport;

import java.net.InetSocketAddress;
import java.util.Collection;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;
import com.ctg.itrdc.janus.remoting.Server;

/**
 * ServerDelegate
 * 
 * @author william.liangf
 */
public class ServerDelegate implements Server {
    
    private transient Server server;
    
    public ServerDelegate() {
    }

    public ServerDelegate(Server server){
        setServer(server);
    }

    public Server getServer() {
        return server;
    }
    
    public void setServer(Server server) {
        this.server = server;
    }

    public boolean isBound() {
        return server.isBound();
    }

    public void reset(URL url) {
        server.reset(url);
    }
    
    @Deprecated
    public void reset(com.ctg.itrdc.janus.common.Parameters parameters){
        reset(getUrl().addParameters(parameters.getParameters()));
    }

    public Collection<Channel> getChannels() {
        return server.getChannels();
    }

    public Channel getChannel(InetSocketAddress remoteAddress) {
        return server.getChannel(remoteAddress);
    }

    public URL getUrl() {
        return server.getUrl();
    }

    public ChannelHandler getChannelHandler() {
        return server.getChannelHandler();
    }

    public InetSocketAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    public void send(Object message) throws RemotingException {
        server.send(message);
    }

    public void send(Object message, boolean sent) throws RemotingException {
        server.send(message, sent);
    }

    public void close() {
        server.close();
    }
    
    public void close(int timeout) {
        server.close(timeout);
    }

    public boolean isClosed() {
        return server.isClosed();
    }
}