package com.ctg.itrdc.janus.common.serialize.support.nativejava;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.serialize.ObjectInput;
import com.ctg.itrdc.janus.common.serialize.ObjectOutput;
import com.ctg.itrdc.janus.common.serialize.Serialization;

/**
 * 原生java支持的序列化
 * 
 * @author Administrator
 */
public class NativeJavaSerialization implements Serialization {

	public static final String NAME = "nativejava";

	public byte getContentTypeId() {
		return 7;
	}

	public String getContentType() {
		return "x-application/nativejava";
	}

	public ObjectOutput serialize(URL url, OutputStream output)
			throws IOException {
		return new NativeJavaObjectOutput(output);
	}

	public ObjectInput deserialize(URL url, InputStream input)
			throws IOException {
		return new NativeJavaObjectInput(input);
	}
}
