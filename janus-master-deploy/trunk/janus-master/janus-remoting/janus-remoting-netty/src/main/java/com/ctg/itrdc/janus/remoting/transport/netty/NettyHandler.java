package com.ctg.itrdc.janus.remoting.transport.netty;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.ChannelHandler;
import com.ctg.itrdc.janus.remoting.RemotingException;
import io.netty.channel.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@io.netty.channel.ChannelHandler.Sharable
public class NettyHandler extends ChannelHandlerAdapter implements
		ChannelOutboundHandler, ChannelInboundHandler {

	private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); // <ip:port,
																							// channel>

	private final URL url;

	private final ChannelHandler handler;

	public NettyHandler(URL url, ChannelHandler handler) {
		if (url == null) {
			throw new IllegalArgumentException("url == null");
		}
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}
		this.url = url;
		this.handler = handler;
	}

	public Map<String, Channel> getChannels() {
		return channels;
	}

	public void bind(ChannelHandlerContext ctx, SocketAddress socketAddress,
			ChannelPromise channelPromise) throws Exception {
		ctx.bind(socketAddress, channelPromise);
	}

	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception {
		ctx.connect(remoteAddress, localAddress, promise);
	}

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		io.netty.channel.Channel c = ctx.channel();
		NettyChannel channel = NettyChannel.getOrAddChannel(c, url, handler);

		try {
			if (channel != null) {
				channels.put(NetUtils.toAddressString((InetSocketAddress) ctx
						.channel().remoteAddress()), channel);
			}
			handler.connected(channel);
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
	}

	public void disconnect(io.netty.channel.ChannelHandlerContext ctx,
			io.netty.channel.ChannelPromise channelPromise)
			throws Exception {
		ctx.disconnect(channelPromise);
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url,
				handler);
		try {
			channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx
					.channel().remoteAddress()));
			handler.disconnected(channel);
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
		ctx.fireChannelInactive();
	}

	public void channelRead(ChannelHandlerContext ctx, final Object msg)
			throws Exception {
		final NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url,handler);
		try {
			ctx.channel().eventLoop().execute(new Runnable() {
				public void run() {
					try {
						handler.received(channel, msg);
					} catch (RemotingException e) {
						throw new RuntimeException(e);
					}
				}
			});

		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
		ctx.fireChannelRead(msg);
	}

	public void write(ChannelHandlerContext ctx, final Object msg,ChannelPromise promise) throws Exception {
		ctx.writeAndFlush(msg, promise);
		final NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url,handler);
		try {
			ctx.channel().eventLoop().execute(new Runnable() {
				public void run() {
					try {
						handler.sent(channel, msg);
					} catch (RemotingException e) {
						throw new RuntimeException(e);
					}
				}
			});

		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url,handler);
		try {
			handler.caught(channel, cause);
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
		// ctx.close();
	}

	public void close(ChannelHandlerContext ctx, ChannelPromise channelPromise)
			throws Exception {
		ctx.close(channelPromise);
	}

	public void deregister(ChannelHandlerContext ctx,
			ChannelPromise channelPromise) throws Exception {
		ctx.deregister(channelPromise);
	}

	public void read(ChannelHandlerContext channelHandlerContext)
			throws Exception {
		channelHandlerContext.read();
	}

	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelRegistered();
	}

	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelUnregistered();
	}

	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelReadComplete();
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object o)
			throws Exception {
		ctx.fireUserEventTriggered(o);
	}

	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		ctx.fireChannelWritabilityChanged();
	}

	public void flush(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
}