package com.ctg.itrdc.janus.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.RpcInvocation;

/**
 * Invoker代理处理
 * 
 * @author Administrator
 */
public class InvokerInvocationHandler implements InvocationHandler {

	/**
	 * 代理调用
	 */
	private final Invoker<?> invoker;

	public InvokerInvocationHandler(Invoker<?> handler) {
		this.invoker = handler;
	}

	/**
	 * 处理调用
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (method.getDeclaringClass() == Object.class) {
			return method.invoke(invoker, args);
		}
		if ("toString".equals(methodName) && parameterTypes.length == 0) {
			return invoker.toString();
		}
		if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
			return invoker.hashCode();
		}
		if ("equals".equals(methodName) && parameterTypes.length == 1) {
			return invoker.equals(args[0]);
		}
		return invoker.invoke(new RpcInvocation(method, args)).recreate();
	}

}