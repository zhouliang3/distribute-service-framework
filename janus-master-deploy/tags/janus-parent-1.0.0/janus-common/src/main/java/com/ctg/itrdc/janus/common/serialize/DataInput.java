package com.ctg.itrdc.janus.common.serialize;

import java.io.IOException;

/**
 * 数据输入流接口
 * 
 * @author Administrator
 */
public interface DataInput {

	/**
	 * 读取boolean值
	 * 
	 * @return boolean.
	 * @throws IOException
	 */
	boolean readBool() throws IOException;

	
	/**
	 * 读取 byte值
	 * @return
	 * @throws IOException
	 */
	byte readByte() throws IOException;

	/**
	 * Read short类型的值.
	 * 
	 * @return short.
	 * @throws IOException
	 */
	short readShort() throws IOException;

	/**
	 * Read 整形值.
	 * 
	 * @return integer.
	 * @throws IOException
	 */
	int readInt() throws IOException;

	/**
	 * Read long值
	 * 
	 * @return long.
	 * @throws IOException
	 */
	long readLong() throws IOException;

	/**
	 * Read float值
	 * 
	 * @return float.
	 * @throws IOException
	 */
	float readFloat() throws IOException;

	/**
	 * Read double值
	 * 
	 * @return double.
	 * @throws IOException
	 */
	double readDouble() throws IOException;

	/**
	 * Read UTF-8编码格式的字符串.
	 * 
	 * @return string.
	 * @throws IOException
	 */
	String readUTF() throws IOException;

	/**
	 * Read byte数组值.
	 * 
	 * @return byte array.
	 * @throws IOException
	 */
	byte[] readBytes() throws IOException;
}