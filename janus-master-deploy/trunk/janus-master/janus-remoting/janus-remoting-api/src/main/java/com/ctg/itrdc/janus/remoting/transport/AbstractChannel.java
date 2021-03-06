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

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;

/**
 * AbstractChannel
 * 
 *
 */
public abstract class AbstractChannel extends AbstractPeer implements Channel {

    public AbstractChannel(URL url, ChannelHandler handler){
        super(url, handler);
    }

    public void send(Object message, boolean sent) throws RemotingException {
        if (isClosed()) {
            throw new RemotingException(this, "Failed to send message "
                                              + (message == null ? "" : message.getClass().getName()) + ":" + message
                                              + ", cause: Channel closed. channel: " + getLocalAddress() + " -> " + getRemoteAddress());
        }
    }

    @Override
    public String toString() {
        return getLocalAddress() + " -> " + getRemoteAddress();
    }

}