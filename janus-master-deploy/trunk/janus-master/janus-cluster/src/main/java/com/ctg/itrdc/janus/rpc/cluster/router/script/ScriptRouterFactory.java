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
package com.ctg.itrdc.janus.rpc.cluster.router.script;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.rpc.cluster.Router;
import com.ctg.itrdc.janus.rpc.cluster.RouterFactory;

/**
 * ScriptRouterFactory
 * 
 * Script Router Factory用到的URL形如：
 * <ol>
 * <li> script://registyAddress?type=js&rule=xxxx
 * <li> script:///path/to/routerfile.js?type=js&rule=xxxx
 * <li> script://D:\path\to\routerfile.js?type=js&rule=xxxx
 * <li> script://C:/path/to/routerfile.js?type=js&rule=xxxx
 * </ol>
 * URL的Host一段包含的是Script Router内容的来源，Registry、File etc
 * 
 *
 */
public class ScriptRouterFactory implements RouterFactory {
    
    public static final String NAME = "script";

    public Router getRouter(URL url) {
        return new ScriptRouter(url);
    }

}