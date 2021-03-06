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

import java.io.IOException;
import java.net.InetSocketAddress;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.serialize.Serialization;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.Codec2;

/**
 * AbstractCodec
 * 
 *
 */
public abstract class AbstractCodec implements Codec2 {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractCodec.class);

    protected Serialization getSerialization(Channel channel) {
        return CodecSupport.getSerialization(channel.getUrl());
    }

    protected static void checkPayload(Channel channel, long size) throws IOException {
        int payload = Constants.DEFAULT_PAYLOAD;
        if (channel != null && channel.getUrl() != null) {
            payload = channel.getUrl().getParameter(Constants.PAYLOAD_KEY, Constants.DEFAULT_PAYLOAD);
        }
        if (payload > 0 && size > payload) {
        	IOException e = new IOException("Data length too large: " + size + ", max payload: " + payload + ", channel: " + channel);
        	logger.error(e);
            throw e;
        }
    }

	protected boolean isClientSide(Channel channel) {
		String side = (String) channel.getAttribute(Constants.SIDE_KEY);
		if ("client".equals(side)) {
			return true;
		} else if ("server".equals(side)) {
			return false;
		} else {
			InetSocketAddress address = channel.getRemoteAddress();
			URL url = channel.getUrl();
			boolean client = url.getPort() == address.getPort()
					&& NetUtils.filterLocalHost(url.getIp()).equals(
							NetUtils.filterLocalHost(address.getAddress()
									.getHostAddress()));
			channel.setAttribute(Constants.SIDE_KEY, client ? "client"
					: "server");
			return client;
		}
	}

	protected boolean isServerSide(Channel channel) {
		return !isClientSide(channel);
	}

}
