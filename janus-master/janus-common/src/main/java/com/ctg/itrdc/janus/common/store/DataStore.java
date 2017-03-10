package com.ctg.itrdc.janus.common.store;

import java.util.Map;

import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * 数据存储
 * 
 * @author Administrator
 */
@SPI("simple")
public interface DataStore {

	/**
	 * 根据组件名，获取属于该组件的map对象
	 * 
	 * @param componentName
	 * @return
	 */
	Map<String, Object> get(String componentName);

	/**
	 * 根据组件名和key，获取所属的对象
	 * 
	 * @param componentName
	 * @param key
	 * @return
	 */
	Object get(String componentName, String key);

	/**
	 * 根据key和vlaue，存放到对应的组件当中
	 * 
	 * @param componentName
	 * @param key
	 * @param value
	 */
	void put(String componentName, String key, Object value);

	/**
	 * 删除组件中的key
	 * 
	 * @param componentName
	 * @param key
	 */
	void remove(String componentName, String key);

}
