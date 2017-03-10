package com.ctg.itrdc.janus.remoting.transport.netty;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.Codec2;
import com.ctg.itrdc.janus.remoting.buffer.DynamicChannelBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;


final class NettyCodecAdapter {

    private final ChannelHandler encoder = new InternalEncoder();

    private final ChannelHandler decoder = new InternalDecoder();

    private final Codec2 codec;

    private final URL url;

    private final int bufferSize;

    private final com.ctg.itrdc.janus.remoting.ChannelHandler handler;

    public NettyCodecAdapter(Codec2 codec, URL url, com.ctg.itrdc.janus.remoting.ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
        int b = url.getPositiveParameter(Constants.BUFFER_KEY, Constants.DEFAULT_BUFFER_SIZE);
        this.bufferSize = b >= Constants.MIN_BUFFER_SIZE && b <= Constants.MAX_BUFFER_SIZE ? b : Constants.DEFAULT_BUFFER_SIZE;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    @Sharable
    private class InternalEncoder extends MessageToMessageEncoder<Object> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Object message, List<Object> out) throws Exception {
            com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.dynamicBuffer(1024, NettyBackedChannelBufferFactory.getInstance());
            //com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.dynamicBuffer(1024);

            Channel ch = ctx.channel();
            NettyChannel channel = NettyChannel.getOrAddChannel(ch, url, handler);
            try {
                codec.encode(channel, buffer, message);
                if (buffer.readableBytes() > 0) {
                    out.add(ctx.alloc().buffer(buffer.readableBytes()).writeBytes(buffer.toByteBuffer()));
                }
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
    }

    private class InternalDecoder extends SimpleChannelInboundHandler<ByteBuf> {


        private com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer buffer =
                com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;

        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf input) throws Exception {
            int readable = input.readableBytes();
            if (readable <= 0) return;
            com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer message;
            if (buffer.readable()) {
                if (buffer instanceof DynamicChannelBuffer) {
                    buffer.writeBytes(input.nioBuffer());
                    message = buffer;
                } else {
                    int size = buffer.readableBytes() + input.readableBytes();
                    message = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.dynamicBuffer(
                            size > bufferSize ? size : bufferSize);
                    message.writeBytes(buffer, buffer.readableBytes());
                    message.writeBytes(input.nioBuffer());
                }
            } else {
                message = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.dynamicBuffer(input.readableBytes(), NettyBackedChannelBufferFactory.getInstance());

                //message = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.dynamicBuffer(input.readableBytes());
                message.writeBytes(input.nioBuffer());
            }

            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
            Object msg;
            int saveReaderIndex;

            try {
                // decode object.
                do {
                    saveReaderIndex = message.readerIndex();
                    try {
                        msg = codec.decode(channel, message);
                    } catch (IOException e) {
                        buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == message.readerIndex()) {
                            buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                            throw new IOException("Decode without read data.");
                        }
                        if (msg != null) {
                            ctx.fireChannelRead(msg);
                        }
                    }
                } while (message.readable());
            } finally {
                if (message.readable()) {
                    message.discardReadBytes();
                    buffer = message;
                } else {
                    buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                }
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.fireExceptionCaught(cause);
        }
    }
}