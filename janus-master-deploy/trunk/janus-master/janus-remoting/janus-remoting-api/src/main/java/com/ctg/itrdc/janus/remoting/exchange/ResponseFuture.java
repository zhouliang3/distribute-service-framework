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
package com.ctg.itrdc.janus.remoting.exchange;

import com.ctg.itrdc.janus.remoting.RemotingException;

/**
 * Future. (API/SPI, Prototype, ThreadSafe)
 * 
 * @see com.ctg.itrdc.janus.remoting.exchange.ExchangeChannel#request(Object)
 * @see com.ctg.itrdc.janus.remoting.exchange.ExchangeChannel#request(Object, int)
 *
 *
 */
public interface ResponseFuture {

    /**
     * get result.
     * 
     * @return result.
     */
    Object get() throws RemotingException;

    /**
     * get result with the specified timeout.
     * 
     * @param timeoutInMillis timeout.
     * @return result.
     */
    Object get(int timeoutInMillis) throws RemotingException;

    /**
     * set callback.
     * 
     * @param callback
     */
    void setCallback(ResponseCallback callback);

    /**
     * check is done.
     * 
     * @return done or not.
     */
    boolean isDone();

}