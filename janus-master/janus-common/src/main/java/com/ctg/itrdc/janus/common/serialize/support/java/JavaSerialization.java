package com.ctg.itrdc.janus.common.serialize.support.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.serialize.ObjectInput;
import com.ctg.itrdc.janus.common.serialize.ObjectOutput;
import com.ctg.itrdc.janus.common.serialize.Serialization;

/**
 * Java自带的序列化
 * 
 * @author Administrator
 */
public class JavaSerialization implements Serialization {

	public byte getContentTypeId() {
		return 3;
	}

	public String getContentType() {
		return "x-application/java";
	}

	public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
		return new JavaObjectOutput(out);
	}

	public ObjectInput deserialize(URL url, InputStream is) throws IOException {
		return new JavaObjectInput(is);
	}

}