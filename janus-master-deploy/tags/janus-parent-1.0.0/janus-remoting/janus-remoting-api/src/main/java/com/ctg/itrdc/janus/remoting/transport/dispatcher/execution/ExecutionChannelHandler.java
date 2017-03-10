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
package com.ctg.itrdc.janus.remoting.transport.dispatcher.execution;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;
import com.ctg.itrdc.janus.remoting.transport.dispatcher.ChannelEventRunnable;
import com.ctg.itrdc.janus.remoting.transport.dispatcher.WrappedChannelHandler;
import com.ctg.itrdc.janus.remoting.transport.dispatcher.ChannelEventRunnable.ChannelState;

public class ExecutionChannelHandler extends WrappedChannelHandler {
    
    public ExecutionChannelHandler(ChannelHandler handler, URL url) {
        super(handler, url);
    }

    public void connected(Channel channel) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CONNECTED));
    }

    public void disconnected(Channel channel) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.DISCONNECTED));
    }

    public void received(Channel channel, Object message) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler, ChannelState.RECEIVED, message));
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CAUGHT, exception));
    }

}