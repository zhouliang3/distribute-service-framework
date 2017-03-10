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
package com.ctg.itrdc.janus.remoting.http;

import java.net.InetSocketAddress;

import com.ctg.itrdc.janus.common.Resetable;
import com.ctg.itrdc.janus.common.URL;

public interface HttpServer extends Resetable {
    
    /**
     * get http handler.
     * 
     * @return http handler.
     */
    HttpHandler getHttpHandler();
    
    /**
     * get url.
     * 
     * @return url
     */
    URL getUrl();
    
    /**
     * get local address.
     * 
     * @return local address.
     */
    InetSocketAddress getLocalAddress();
    
    /**
     * close the channel.
     */
    void close();
    
    /**
     * Graceful close the channel.
     */
    void close(int timeout);
    
    /**
     * is bound.
     * 
     * @return bound
     */
    boolean isBound();
    
    /**
     * is closed.
     * 
     * @return closed
     */
    boolean isClosed();
    
}