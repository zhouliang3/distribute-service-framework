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
package com.ctg.itrdc.janus.remoting.transport;

import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;

/**
 * ChannelHandlerAdapter.
 * 
 *
 */
public class ChannelHandlerAdapter implements ChannelHandler {

    public void connected(Channel channel) throws RemotingException {
    }

    public void disconnected(Channel channel) throws RemotingException {
    }

    public void sent(Channel channel, Object message) throws RemotingException {
    }

    public void received(Channel channel, Object message) throws RemotingException {
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
    }

}