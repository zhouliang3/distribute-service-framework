package com.ctg.itrdc.janus.remoting.transport.netty5;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.remoting.Codec2;
import com.ctg.itrdc.janus.remoting.buffer.DynamicChannelBuffer;
import com.ctg.itrdc.janus.remoting.transport.netty5.logutil.NettyHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;


public class NettyCodecAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyCodecAdapter.class);

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
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            logger.info("Message to encode is :" + msg);
            com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer buffer =
                    com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.dynamicBuffer(1024);
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
            try {
                codec.encode(channel, buffer, msg);
                logger.info("Message is encoded to :" + buffer.array().toString());

                logger.info("Thread " + Thread.currentThread().getName() + "Message is sended over :" + buffer.array().toString());
                System.out.println("InternalEncoder-----------------");

                NettyHelper.printThreadInfo(Thread.currentThread());
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
            out.add(Unpooled.wrappedBuffer(buffer.toByteBuffer()));

        }

    }

    private class InternalDecoder extends SimpleChannelInboundHandler<ByteBuf> {
        private com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer buffer =
                com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, ByteBuf input) throws Exception {
            System.out.println("InternalDecoder-----------------");

            NettyHelper.printThreadInfo(Thread.currentThread());
            int readable = input.readableBytes();
            if (readable <= 0) {
                return;
            }
            com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer message;
            if (buffer.readable()) {//inote 将buffer中缓存的数据和input进来的数据合并
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
                message = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.wrappedBuffer(
                        input.nioBuffer());
            }
            Object tmpMsg;
            int saveReaderIndex;
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
            try {
                // decode object.
                do {
                    saveReaderIndex = message.readerIndex();
                    try {
                        tmpMsg = codec.decode(channel, message);
                    } catch (IOException e) {
                        buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                        throw e;
                    }
                    if (tmpMsg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == message.readerIndex()) {
                            buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                            throw new IOException("Decode without read data.");
                        }
                        if (tmpMsg != null) {
                            ctx.fireChannelRead(tmpMsg);//iundo 如何保证此时传入的是ByteBuf
                        }
                    }
                } while (message.readable());
            } finally {
                if (message.readable()) {//inote 拆包的情况下，会反回NEED_MORE_INPUT，然后回滚readerIndex，此时就变成了readable==true了
                    message.discardReadBytes();//inote 方法执行之后，message中只剩下readable btyes
                    buffer = message;//iundo 将message赋给buffer，那么buffer缓存了此次received方法调用所读取到的数据，下次再调用received方法时，会将新读取到的数据与此buffer合并
                } else {
                    buffer = com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;//inote 如果接收到的消息 包含且仅包含 一个是完整的协议栈，那么当前buffer通过ExchangeCodec解析协议之后，当前的buffer的readeIndex位置应该是buffer尾部，那么在返回到InternalDecoder中message的方法readable返回的是false,那么就会对buffer重新赋予EMPTY_BUFFER实体
                }
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.fireChannelRead(cause);
        }
    }
}
