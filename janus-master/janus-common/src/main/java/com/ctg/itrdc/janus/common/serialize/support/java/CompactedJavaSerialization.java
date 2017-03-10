package com.ctg.itrdc.janus.common.serialize.support.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.serialize.ObjectInput;
import com.ctg.itrdc.janus.common.serialize.ObjectOutput;
import com.ctg.itrdc.janus.common.serialize.Serialization;

/**
 * 紧凑型的Java原生支持序列化
 * 
 * @author Administrator
 */
public class CompactedJavaSerialization implements Serialization {

	public byte getContentTypeId() {
		return 4;
	}

	public String getContentType() {
		return "x-application/compactedjava";
	}

	public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
		return new JavaObjectOutput(out, true);
	}

	public ObjectInput deserialize(URL url, InputStream is) throws IOException {
		return new JavaObjectInput(is, true);
	}

}