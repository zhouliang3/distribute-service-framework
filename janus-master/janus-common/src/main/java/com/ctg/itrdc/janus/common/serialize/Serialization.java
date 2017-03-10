package com.ctg.itrdc.janus.common.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 序列化接口
 * 
 * @author Administrator
 */
@SPI("hessian2")
public interface Serialization {

	/**
	 * get content type id
	 * 
	 * @return content type id
	 */
	byte getContentTypeId();

	/**
	 * get content type
	 * 
	 * @return content type
	 */
	String getContentType();

	/**
	 * create serializer
	 * 
	 * @param url
	 * @param output
	 * @return serializer
	 * @throws IOException
	 */
	@Adaptive
	ObjectOutput serialize(URL url, OutputStream output) throws IOException;

	/**
	 * create deserializer
	 * 
	 * @param url
	 * @param input
	 * @return deserializer
	 * @throws IOException
	 */
	@Adaptive
	ObjectInput deserialize(URL url, InputStream input) throws IOException;

}