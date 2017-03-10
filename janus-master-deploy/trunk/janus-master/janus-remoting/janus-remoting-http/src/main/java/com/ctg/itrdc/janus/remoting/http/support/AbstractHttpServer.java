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
package com.ctg.itrdc.janus.remoting.http.support;

import java.net.InetSocketAddress;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.http.HttpHandler;
import com.ctg.itrdc.janus.remoting.http.HttpServer;

/**
 * AbstractHttpServer
 * 
 *
 */
public abstract class AbstractHttpServer implements HttpServer {

    private final URL url;
    
    private final HttpHandler handler;

    private volatile boolean closed;
    
    public AbstractHttpServer(URL url, HttpHandler handler){
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }
    
    public HttpHandler getHttpHandler() {
        return handler;
    }

    public URL getUrl() {
        return url;
    }

    public void reset(URL url) {
    }
    
    public boolean isBound() {
        return true;
    }

    public InetSocketAddress getLocalAddress() {
        return url.toInetSocketAddress();
    }

    public void close() {
        closed = true;
    }

    public void close(int timeout) {
        close();
    }

    public boolean isClosed() {
        return closed;
    }

}