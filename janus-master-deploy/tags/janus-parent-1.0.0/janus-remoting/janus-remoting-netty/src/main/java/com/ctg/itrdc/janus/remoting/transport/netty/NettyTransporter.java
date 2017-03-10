package com.ctg.itrdc.janus.remoting.transport.netty;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.*;


public class NettyTransporter implements Transporter {

    public static final String NAME = "netty";
    
    public Server bind(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyServer(url, listener);
    }

    public Client connect(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyClient(url, listener);
    }

}