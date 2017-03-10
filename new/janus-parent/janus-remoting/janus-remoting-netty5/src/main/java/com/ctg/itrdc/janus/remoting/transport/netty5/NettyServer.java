package com.ctg.itrdc.janus.remoting.transport.netty5;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.ExecutorUtil;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;
import com.ctg.itrdc.janus.remoting.Server;
import com.ctg.itrdc.janus.remoting.transport.AbstractServer;
import com.ctg.itrdc.janus.remoting.transport.dispatcher.ChannelHandlers;
import com.ctg.itrdc.janus.remoting.transport.netty5.logutil.NettyHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;


public class NettyServer extends AbstractServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private Map<String, Channel> channels; // <ip:port -> channel>

    private io.netty.channel.Channel channel;
    ServerBootstrap bootstrap;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
    }

    @Override
    protected void doOpen() throws Throwable {
        System.out.println("Server-----------------");
        NettyHelper.printThreadInfo(Thread.currentThread());

        NettyHelper.setNettyLoggerFactory();
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final NettyHandler nettyHandler = new NettyHandler(getUrl(), this);
        channels = nettyHandler.getChannels();
        //iundo 这段代码需要修改参数，解码器等
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)//TODO SO_BACKLOG参数的作用
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch)
                            throws IOException {
                        NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyServer.this);
                        ch.pipeline().addLast("decoder", adapter.getDecoder());
                        ch.pipeline().addLast("encoder", adapter.getEncoder());
                        ch.pipeline().addLast("handler", nettyHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(getBindAddress()).sync();//inote 线程同步阻塞等待服务器绑定到指定端口,
        channelFuture.awaitUninterruptibly();
        //channelFuture.channel().closeFuture().sync();//inote 应用程序会一直等待，直到channel关闭 需删除
        channel = channelFuture.channel();
    }

    @Override
    protected void doClose() throws Throwable {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<com.ctg.itrdc.janus.remoting.Channel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (com.ctg.itrdc.janus.remoting.Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            // release external resource.
            ///inote netty 5 中的的bootstrap没有资源释放方法.
            // inote 优雅退出，释放线程池资源
            if (null != bossGroup) {
                bossGroup.shutdownGracefully();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }
}
