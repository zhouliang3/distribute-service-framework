package com.ctg.itrdc.janus.rpc.filter;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.RpcResult;

/**
 * 回声测试用于检测服务是否可用，回声测试按照正常请求流程执行，能够测试整个调用是否通畅，可用于监控。
 * 所有服务自动实现EchoService接口，只需将任意服务引用强制转型为EchoService，即可使用。<br/>
 * 
 * 例如
 * 
 * <pre>
 *  <janus:reference id="memberService" interface="com.xxx.MemberService" />
 *  MemberService memberService = ctx.getBean("memberService"); // 远程服务引用
 * 
 *  EchoService echoService = (EchoService) memberService; // 强制转型为EchoService
 * 
 *  String status = echoService.$echo("OK"); // 回声测试可用性
 * 
 *  assert(status.equals("OK"))
 * 
 * </pre>
 * 
 * @author Administrator
 */
@Activate(group = Constants.PROVIDER, order = -110000)
public class EchoFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation inv)
			throws RpcException {
		if (inv.getMethodName().equals(Constants.$ECHO)
				&& inv.getArguments() != null && inv.getArguments().length == 1)
			return new RpcResult(inv.getArguments()[0]);
		return invoker.invoke(inv);
	}

}