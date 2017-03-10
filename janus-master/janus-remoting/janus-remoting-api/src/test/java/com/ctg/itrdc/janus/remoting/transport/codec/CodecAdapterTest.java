package com.ctg.itrdc.janus.remoting.transport.codec;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.io.UnsafeByteArrayInputStream;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.Codec;
import com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer;
import com.ctg.itrdc.janus.remoting.buffer.ChannelBuffers;
import com.ctg.itrdc.janus.remoting.codec.ExchangeCodecTest;
import com.ctg.itrdc.janus.remoting.telnet.codec.TelnetCodec;

import junit.framework.Assert;

/**
 * @author <a href="mailto:gang.lvg@taobao.com">kimi</a>
 */
public class CodecAdapterTest extends ExchangeCodecTest {

    @Before
    public void setUp() throws Exception {
        codec = new CodecAdapter(new DeprecatedExchangeCodec());
    }

}
