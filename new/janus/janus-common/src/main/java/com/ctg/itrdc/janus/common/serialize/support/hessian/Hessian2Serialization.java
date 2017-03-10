package com.ctg.itrdc.janus.common.serialize.support.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.serialize.ObjectInput;
import com.ctg.itrdc.janus.common.serialize.ObjectOutput;
import com.ctg.itrdc.janus.common.serialize.Serialization;

/**
 * Hessian2序列化
 * 
 * @author Administrator
 */
public class Hessian2Serialization implements Serialization {

	public static final byte ID = 2;

	public byte getContentTypeId() {
		return ID;
	}

	public String getContentType() {
		return "x-application/hessian2";
	}

	public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
		return new Hessian2ObjectOutput(out);
	}

	public ObjectInput deserialize(URL url, InputStream is) throws IOException {
		return new Hessian2ObjectInput(is);
	}

}