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
package com.ctg.itrdc.janus.remoting;

import java.net.InetSocketAddress;

/**
 * TimeoutException. (API, Prototype, ThreadSafe)
 * 
 * @see com.ctg.itrdc.janus.remoting.exchange.ResponseFuture#get()
 * @see com.ctg.itrdc.janus.remoting.exchange.ResponseFuture#get(int)
 *
 * @export
 */
public class TimeoutException extends RemotingException {

    private static final long serialVersionUID = 3122966731958222692L;
    
    public static final int CLIENT_SIDE = 0;
    
    public static final int SERVER_SIDE = 1;

    private final int       phase;

    public TimeoutException(boolean serverSide, Channel channel, String message){
        super(channel, message);
        this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
    }

    public TimeoutException(boolean serverSide, InetSocketAddress localAddress, 
                            InetSocketAddress remoteAddress, String message) {
        super(localAddress, remoteAddress, message);
        this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isServerSide() {
        return phase == 1;
    }

    public boolean isClientSide() {
        return phase == 0;
    }

}