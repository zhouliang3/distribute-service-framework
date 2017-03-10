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
package com.ctg.itrdc.janus.remoting.transport.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ctg.itrdc.janus.common.serialize.Cleanable;
import com.ctg.itrdc.janus.common.serialize.ObjectInput;
import com.ctg.itrdc.janus.common.serialize.ObjectOutput;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.remoting.Channel;
import com.ctg.itrdc.janus.remoting.buffer.ChannelBuffer;
import com.ctg.itrdc.janus.remoting.buffer.ChannelBufferInputStream;
import com.ctg.itrdc.janus.remoting.buffer.ChannelBufferOutputStream;
import com.ctg.itrdc.janus.remoting.transport.AbstractCodec;

/**
 * TransportCodec
 * 
 *
 */
public class TransportCodec extends AbstractCodec {

    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        OutputStream output = new ChannelBufferOutputStream(buffer);
        ObjectOutput objectOutput = getSerialization(channel).serialize(channel.getUrl(), output);
        encodeData(channel, objectOutput, message);
        objectOutput.flushBuffer();

        // modified by lishen
        if (objectOutput instanceof Cleanable) {
            ((Cleanable) objectOutput).cleanup();
        }
    }

    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
//        InputStream input = new ChannelBufferInputStream(buffer);
//        return decodeData(channel, getSerialization(channel).deserialize(channel.getUrl(), input));

        // modified by lishen
        InputStream input = new ChannelBufferInputStream(buffer);
        ObjectInput objectInput = getSerialization(channel).deserialize(channel.getUrl(), input);
        Object object = decodeData(channel, objectInput);
        if (objectInput instanceof Cleanable) {
            ((Cleanable) objectInput).cleanup();
        }
        return object;
    }

    protected void encodeData(Channel channel, ObjectOutput output, Object message) throws IOException {
        encodeData(output, message);
    }

    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
        return decodeData(input);
    }

    protected void encodeData(ObjectOutput output, Object message) throws IOException {
        output.writeObject(message);
    }

    protected Object decodeData(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
        }
    }
}