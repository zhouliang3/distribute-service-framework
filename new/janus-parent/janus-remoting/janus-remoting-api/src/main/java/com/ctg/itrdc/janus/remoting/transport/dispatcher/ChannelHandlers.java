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
package com.ctg.itrdc.janus.remoting.transport.dispatcher;


import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.Dispatcher;
import com.ctg.itrdc.janus.remoting.exchange.support.header.HeartbeatHandler;
import com.ctg.itrdc.janus.remoting.transport.MultiMessageHandler;

/**
 * @author chao.liuc
 *
 */
public class ChannelHandlers {

    public static ChannelHandler wrap(ChannelHandler handler, URL url){
        return ChannelHandlers.getInstance().wrapInternal(handler, url);
    }

    protected ChannelHandlers() {}

    protected ChannelHandler wrapInternal(ChannelHandler handler, URL url) {
        //iundo 注释掉线程派发的部分
        //return new MultiMessageHandler(new HeartbeatHandler(handler));

        return new MultiMessageHandler(new HeartbeatHandler(ExtensionLoader.getExtensionLoader(Dispatcher.class).getAdaptiveExtension().dispatch(handler, url)));
    }

    private static ChannelHandlers INSTANCE = new ChannelHandlers();

    protected static ChannelHandlers getInstance() {
        return INSTANCE;
    }

    static void setTestingChannelHandlers(ChannelHandlers instance) {
        INSTANCE = instance;
    }
}