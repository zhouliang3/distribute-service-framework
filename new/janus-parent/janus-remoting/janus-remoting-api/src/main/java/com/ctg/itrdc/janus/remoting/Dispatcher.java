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
package com.ctg.itrdc.janus.remoting;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;
import com.ctg.itrdc.janus.remoting.transport.dispatcher.all.AllDispatcher;

/**
 * ChannelHandlerWrapper (SPI, Singleton, ThreadSafe)
 * 
 * @author chao.liuc
 */
@SPI(AllDispatcher.NAME)
public interface Dispatcher {

    /**
     * dispatch the message to threadpool.
     * 
     * @param handler
     * @param url
     * @return channel handler
     */
    @Adaptive({Constants.DISPATCHER_KEY, "dispather", "channel.handler"}) // 后两个参数为兼容旧配置
    ChannelHandler dispatch(ChannelHandler handler, URL url);

}