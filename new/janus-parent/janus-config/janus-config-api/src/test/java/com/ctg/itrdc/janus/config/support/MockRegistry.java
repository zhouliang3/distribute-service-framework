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
package com.ctg.itrdc.janus.config.support;

import java.util.ArrayList;
import java.util.List;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.registry.NotifyListener;
import com.ctg.itrdc.janus.registry.Registry;

/**
 * TODO Comment of MockRegistry
 * @author haomin.liuhm
 *
 */
public class MockRegistry implements Registry {

    static URL subscribedUrl = new URL("null", "0.0.0.0", 0);
    
    public static URL getSubscribedUrl(){
        return subscribedUrl;
    }
    
    /* 
     * @see com.ctg.itrdc.janus.common.Node#getUrl()
     */
    public URL getUrl() {
        return null;
    }

    /* 
     * @see com.ctg.itrdc.janus.common.Node#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /* 
     * @see com.ctg.itrdc.janus.common.Node#destroy()
     */
    public void destroy() {
        
    }

    /* 
     * @see com.ctg.itrdc.janus.registry.RegistryService#register(com.ctg.itrdc.janus.common.URL)
     */
    public void register(URL url) {
        
    }

    /* 
     * @see com.ctg.itrdc.janus.registry.RegistryService#unregister(com.ctg.itrdc.janus.common.URL)
     */
    public void unregister(URL url) {
        
    }

    /* 
     * @see com.ctg.itrdc.janus.registry.RegistryService#subscribe(com.ctg.itrdc.janus.common.URL, com.ctg.itrdc.janus.registry.NotifyListener)
     */
    public void subscribe(URL url, NotifyListener listener) {
        this.subscribedUrl = url;
        List<URL> urls = new ArrayList<URL>();
        
        urls.add(url.setProtocol("mockprotocol")
                    .removeParameter(Constants.CATEGORY_KEY)
                    .addParameter(Constants.METHODS_KEY, "sayHello"));
        
        listener.notify(urls);
    }

    /* 
     * @see com.ctg.itrdc.janus.registry.RegistryService#unsubscribe(com.ctg.itrdc.janus.common.URL, com.ctg.itrdc.janus.registry.NotifyListener)
     */
    public void unsubscribe(URL url, NotifyListener listener) {
        
    }

    /* 
     * @see com.ctg.itrdc.janus.registry.RegistryService#lookup(com.ctg.itrdc.janus.common.URL)
     */
    public List<URL> lookup(URL url) {
        return null;
    }

}