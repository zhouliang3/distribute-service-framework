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

import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.telnet.TelnetHandler;
import com.ctg.itrdc.janus.remoting.telnet.support.Help;
import com.ctg.itrdc.janus.rpc.Exporter;
import com.ctg.itrdc.janus.rpc.protocol.janus.JanusProtocol;

/**
 * ChangeServiceTelnetHandler
 * 
 * @author william.liangf
 */
@Activate
@Help(parameter = "[service]", summary = "Change default service.", detail = "Change default service.")
public class ChangeTelnetHandler implements TelnetHandler {
    
    public static final String SERVICE_KEY = "telnet.service";

    public String telnet(Channel channel, String message) {
        if (message == null || message.length() == 0) {
            return "Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService";
        }
        StringBuilder buf = new StringBuilder();
        if (message.equals("/") || message.equals("..")) {
            String service = (String) channel.getAttribute(SERVICE_KEY);
            channel.removeAttribute(SERVICE_KEY);
            buf.append("Cancelled default service " + service + ".");
        } else {
            boolean found = false;
            for (Exporter<?> exporter : JanusProtocol.getDubboProtocol().getExporters()) {
                if (message.equals(exporter.getInvoker().getInterface().getSimpleName())
                        || message.equals(exporter.getInvoker().getInterface().getName())
                        || message.equals(exporter.getInvoker().getUrl().getPath())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                channel.setAttribute(SERVICE_KEY, message);
                buf.append("Used the " + message + " as default.\r\nYou can cancel default service by command: cd /");
            } else {
                buf.append("No such service " + message);
            }
        }
        return buf.toString();
    }

}