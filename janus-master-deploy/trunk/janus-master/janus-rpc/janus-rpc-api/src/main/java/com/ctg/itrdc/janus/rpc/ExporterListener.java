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
package com.ctg.itrdc.janus.rpc;

import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * ExporterListener. (SPI, Singleton, ThreadSafe)
 * 
 *
 */
@SPI
public interface ExporterListener {

    /**
     * The exporter exported.
     * 
     * @see com.ctg.itrdc.janus.rpc.Protocol#export(Invoker)
     * @param exporter
     * @throws RpcException
     */
    void exported(Exporter<?> exporter) throws RpcException;

    /**
     * The exporter unexported.
     * 
     * @see com.ctg.itrdc.janus.rpc.Exporter#unexport()
     * @param exporter
     * @throws RpcException
     */
    void unexported(Exporter<?> exporter);

}