package com.ctg.itrdc.janus.common.serialize.serialization;

import com.ctg.itrdc.janus.common.serialize.support.kryo.KryoSerialization;

/**
 * @author lishen
 */
public class KyroSerializationTest extends AbstractSerializationTest {

    {
        serialization = new KryoSerialization();
    }
}