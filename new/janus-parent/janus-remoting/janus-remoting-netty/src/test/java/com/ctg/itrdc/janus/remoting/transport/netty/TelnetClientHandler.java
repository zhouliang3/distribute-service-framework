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
package com.ctg.itrdc.janus.remoting.transport.netty;

import com.ctg.itrdc.janus.remoting.RemotingException;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeChannel;
import com.ctg.itrdc.janus.remoting.exchange.support.Replier;

/**
 * User: heyman
 * Date: 4/28/11
 * Time: 11:15 AM
 */
public class TelnetClientHandler implements Replier<String> {

    public Class<String> interest() {
        return String.class;
    }

    public Object reply(ExchangeChannel channel, String msg) throws RemotingException {
        return msg;
    }
}