package com.ctg.itrdc.janus.remoting.transport.netty;

import com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer;
import com.ctg.itrdc.janus.remoting.buffer.ChannelBufferFactory;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.ByteBuffer;


public class NettyBackedChannelBufferFactory implements ChannelBufferFactory {

    private static final NettyBackedChannelBufferFactory INSTANCE = new NettyBackedChannelBufferFactory();

    public static ChannelBufferFactory getInstance() {
        return INSTANCE;
    }

    public ChannelBuffer getBuffer(int capacity) {
        return new NettyBackedChannelBuffer(PooledByteBufAllocator.DEFAULT.buffer(capacity));
    }

    public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
        io.netty.buffer.ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(length);
        buffer.writeBytes(array, offset, length);
        return new NettyBackedChannelBuffer(buffer);
    }

    public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        return new NettyBackedChannelBuffer(PooledByteBufAllocator.DEFAULT.buffer().writeBytes(nioBuffer));
    }
}
