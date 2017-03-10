package com.ctg.itrdc.janus.common.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Object对象的输入流操作接口.
 * 
 *
 */
public interface ObjectInput extends DataInput {

	/**
	 * 读取对象.
	 * 
	 * @return object.
	 */
	Object readObject() throws IOException, ClassNotFoundException;

	/**
	 * read 对象.
	 * 
	 * @param cls
	 *            object type.
	 * @return object.
	 */
	<T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException;

	/**
	 * read 对象.
	 * 
	 * @param cls
	 *            object type.
	 * @return object.
	 */
	<T> T readObject(Class<T> cls, Type type) throws IOException,
			ClassNotFoundException;

}