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

import com.ctg.itrdc.janus.common.Resetable;

/**
 * Remoting Client. (API/SPI, Prototype, ThreadSafe)
 * 
 * <a href="http://en.wikipedia.org/wiki/Client%E2%80%93server_model">Client/Server</a>
 * 
 * @see com.ctg.itrdc.janus.remoting.Transporter#connect(com.ctg.itrdc.janus.common.URL, ChannelHandler)
 *
 */
public interface Client extends Endpoint, Channel, Resetable {

    /**
     * reconnect.
     */
    void reconnect() throws RemotingException;
    
    @Deprecated
    void reset(com.ctg.itrdc.janus.common.Parameters parameters);
    
}