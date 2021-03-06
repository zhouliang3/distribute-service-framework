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
package com.ctg.itrdc.janus.rpc.protocol.janus.status;

import java.util.Collection;

import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.status.Status;
import com.ctg.itrdc.janus.common.status.StatusChecker;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeServer;
import com.ctg.itrdc.janus.rpc.protocol.janus.JanusProtocol;

/**
 * ServerStatusChecker
 * 
 *
 */
@Activate
public class ServerStatusChecker implements StatusChecker {

    public Status check() {
        Collection<ExchangeServer> servers = JanusProtocol.getJanusProtocol().getServers();
        if (servers == null || servers.size() == 0) {
            return new Status(Status.Level.UNKNOWN);
        }
        Status.Level level = Status.Level.OK;
        StringBuilder buf = new StringBuilder();
        for (ExchangeServer server : servers) {
            if (! server.isBound()) {
                level = Status.Level.ERROR;
                buf.setLength(0);
                buf.append(server.getLocalAddress());
                break;
            }
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(server.getLocalAddress());
            buf.append("(clients:");
            buf.append(server.getChannels().size());
            buf.append(")");
        }
        return new Status(level, buf.toString());
    }

}