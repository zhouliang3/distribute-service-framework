package com.ctg.itrdc.janus.remoting.transport.netty5;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.*;

/**
 * Created by lenovo on 2016/3/28 0028.${}
 */
public class NettyTransporter implements Transporter {
    public static final String NAME = "netty";

    public Server bind(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyServer(url, listener);
    }

    public Client connect(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyClient(url, listener);
    }
}
