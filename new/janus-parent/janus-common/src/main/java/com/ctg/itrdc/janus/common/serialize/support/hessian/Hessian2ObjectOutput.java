package com.ctg.itrdc.janus.common.serialize.support.hessian;

import java.io.IOException;
import java.io.OutputStream;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.ctg.itrdc.janus.common.serialize.ObjectOutput;

/**
 * Hessian2 对象输出
 * 
 * @author Administrator
 */
public class Hessian2ObjectOutput implements ObjectOutput {
	private final Hessian2Output mH2o;

	public Hessian2ObjectOutput(OutputStream os) {
		mH2o = new Hessian2Output(os);
		mH2o.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
	}

	public void writeBool(boolean v) throws IOException {
		mH2o.writeBoolean(v);
	}

	public void writeByte(byte v) throws IOException {
		mH2o.writeInt(v);
	}

	public void writeShort(short v) throws IOException {
		mH2o.writeInt(v);
	}

	public void writeInt(int v) throws IOException {
		mH2o.writeInt(v);
	}

	public void writeLong(long v) throws IOException {
		mH2o.writeLong(v);
	}

	public void writeFloat(float v) throws IOException {
		mH2o.writeDouble(v);
	}

	public void writeDouble(double v) throws IOException {
		mH2o.writeDouble(v);
	}

	public void writeBytes(byte[] b) throws IOException {
		mH2o.writeBytes(b);
	}

	public void writeBytes(byte[] b, int off, int len) throws IOException {
		mH2o.writeBytes(b, off, len);
	}

	public void writeUTF(String v) throws IOException {
		mH2o.writeString(v);
	}

	public void writeObject(Object obj) throws IOException {
		mH2o.writeObject(obj);
	}

	public void flushBuffer() throws IOException {
		mH2o.flushBuffer();
	}
}