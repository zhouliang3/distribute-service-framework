/*
 * Copyright 1999-2012 Alibaba Group.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.ctg.itrdc.janus.examples.heartbeat;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.remoting.Transporters;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeClient;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeHandler;
import com.ctg.itrdc.janus.remoting.exchange.ExchangeServer;
import com.ctg.itrdc.janus.remoting.exchange.support.ExchangeHandlerAdapter;
import com.ctg.itrdc.janus.remoting.exchange.support.header.HeaderExchangeClient;
import com.ctg.itrdc.janus.remoting.exchange.support.header.HeaderExchangeServer;

/**
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
public class HeartbeatClient {

    private static final URL serverUrl = URL.valueOf(
            new StringBuilder( 32 )
                    .append( "netty://" )
                    .append( NetUtils.getLocalHost() )
                    .append( ":9999" ).toString() )
            .addParameter( Constants.CODEC_KEY, "exchange" );

    private static final ExchangeHandler handler = new ExchangeHandlerAdapter() {

    };

    private static ExchangeServer exchangeServer;
    
    private static volatile boolean serverStarted = false;
    
    public static void main( String[] args ) throws Exception {
        
        final HeartBeatExchangeHandler serverHandler = new HeartBeatExchangeHandler( handler );
        
        Thread serverThread = new Thread( new Runnable() {

            public void run() {
                try {
                    exchangeServer = new HeaderExchangeServer(
                            Transporters.bind( serverUrl, serverHandler ) );
                    serverStarted = true;
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        } );
        
        serverThread.setDaemon( true );
        serverThread.start();
        
        while ( ! serverStarted ) {
            Thread.sleep( 1000 );
        }
        
        URL url = serverUrl.addParameter( Constants.HEARTBEAT_KEY, 1000 );

        HeartBeatExchangeHandler clientHandler = new HeartBeatExchangeHandler( handler );
        ExchangeClient exchangeClient = new HeaderExchangeClient(
                Transporters.connect( url, clientHandler ) );
        
        for( int i = 0; i < 10; i++ ) {
            Thread.sleep( 1000 );
            System.out.print( "." );
        }

        System.out.println();

        if ( serverHandler.getHeartBeatCount() > 0 ) {
            System.out.printf( "Server receives %d heartbeats",
                               serverHandler.getHeartBeatCount() );
        } else {
            throw new Exception( "Client heartbeat does not work." );
        }

        exchangeClient.close();
        exchangeServer.close();

    }

}
