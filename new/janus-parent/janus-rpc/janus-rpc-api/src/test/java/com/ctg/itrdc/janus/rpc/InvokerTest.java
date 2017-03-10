package com.ctg.itrdc.janus.rpc;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;

public class InvokerTest extends Assert {

	private ProxyFactory proxy = ExtensionLoader.getExtensionLoader(
			ProxyFactory.class).getAdaptiveExtension();

	DemoService service = new DemoServiceImpl();

	Invoker<DemoService> invoker = proxy.getInvoker(service, DemoService.class,
			URL.valueOf("rmi://127.0.0.1:9001/TestService"));

	@Test
	public void testGetInterface() {
		Class<?> clazz = invoker.getInterface();
		assertEquals(clazz, DemoService.class);
	}

	@Test
	public void testInvoke() throws Exception {
		String message = "janus";
		Method method = DemoService.class.getMethod("echo", String.class);
		Result result = invoker.invoke(new RpcInvocation(method,
				new Object[] { message }));
		assertTrue(invoker.isAvailable());
		assertEquals(message, result.getValue());

	}

}
