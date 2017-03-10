/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.ctg.itrdc.janus.cache.support.lru;

import com.ctg.itrdc.janus.cache.Cache;
import com.ctg.itrdc.janus.common.URL;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LruCache
 * 
 *
 */
public class LruCache implements Cache {
    
    private final Map<Object, Object> store;

    public LruCache(URL url) {
        final int max = url.getParameter("cache.size", 1000);
        this.store = new LinkedHashMap<Object, Object>() {
            private static final long serialVersionUID = -3834209229668463829L;
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {//iundo 原始版本“Entry<Object, Object>”；编译在本工程报错。需要改为：Map.Entry
                return size() > max;
            }
        };
    }

    public void put(Object key, Object value) {
        synchronized (store) {
            store.put(key, value);
        }
    }

    public Object get(Object key) {
        synchronized (store) {
            return store.get(key);
        }
    }

}
