
package com.ctg.itrdc.janus.rpc;

/**
 * <code>TestService</code>
 */

public interface DemoService
{
	void sayHello(String name);

	String echo(String text);

	long timestamp();
	
	void throwTimeout();

	String getThreadName();

	int getSize(String[] strs);

	int getSize(Object[] os);

	Object invoke(String service, String method) throws Exception;

	int stringLength(String str);

	Type enumlength(Type... types);
}