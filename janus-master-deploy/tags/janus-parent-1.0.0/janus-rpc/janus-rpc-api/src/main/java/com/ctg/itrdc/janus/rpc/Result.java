package com.ctg.itrdc.janus.rpc;

import java.util.Map;

/**
 * RPC 调用结果 (API, Prototype, NonThreadSafe)
 * 
 * @serial Don't change the class name and package name.
 * @see com.ctg.itrdc.janus.rpc.Invoker#invoke(Invocation)
 * @see com.ctg.itrdc.janus.rpc.RpcResult
 * @author Administrator
 */
public interface Result {

	/**
	 * 获取调用后的结果值
	 * 
	 * @return result. if no result return null.
	 */
	Object getValue();

	/**
	 * 获取调用异常
	 * 
	 * @return exception. if no exception return null.
	 */
	Throwable getException();

	/**
	 * 是否有异常抛出
	 * 
	 * @return has exception.
	 */
	boolean hasException();

	/**
	 * Recreate.
	 * 
	 * <code>
	 * if (hasException()) {
	 *     throw getException();
	 * } else {
	 *     return getValue();
	 * }
	 * </code>
	 * 
	 * @return result.
	 * @throws if
	 *             has exception throw it.
	 */
	Object recreate() throws Throwable;

	/**
	 * 获取附件
	 *
	 * @return attachments.
	 */
	Map<String, String> getAttachments();

	/**
	 * 根据key，获取附件
	 *
	 * @return attachment value.
	 */
	String getAttachment(String key);

	/**
	 * get attachment by key with default value.
	 *
	 * @return attachment value.
	 */
	String getAttachment(String key, String defaultValue);

}