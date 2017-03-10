package com.ctg.itrdc.janus.rpc.protocol;

import java.util.Collections;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.rpc.Exporter;
import com.ctg.itrdc.janus.rpc.ExporterListener;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.InvokerListener;
import com.ctg.itrdc.janus.rpc.Protocol;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.listener.ListenerExporterWrapper;
import com.ctg.itrdc.janus.rpc.listener.ListenerInvokerWrapper;

/**
 * 监听器处理Protocol
 * 
 * @author Administrator
 */
public class ProtocolListenerWrapper implements Protocol {

	private final Protocol protocol;

	public ProtocolListenerWrapper(Protocol protocol) {
		if (protocol == null) {
			throw new IllegalArgumentException("protocol == null");
		}
		this.protocol = protocol;
	}

	public int getDefaultPort() {
		return protocol.getDefaultPort();
	}

	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
			return protocol.export(invoker);
		}
		return new ListenerExporterWrapper<T>(protocol.export(invoker),
				Collections.unmodifiableList(ExtensionLoader
						.getExtensionLoader(ExporterListener.class)
						.getActivateExtension(invoker.getUrl(),
								Constants.EXPORTER_LISTENER_KEY)));
	}

	public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
		if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
			return protocol.refer(type, url);
		}
		return new ListenerInvokerWrapper<T>(protocol.refer(type, url),
				Collections.unmodifiableList(ExtensionLoader
						.getExtensionLoader(InvokerListener.class)
						.getActivateExtension(url,
								Constants.INVOKER_LISTENER_KEY)));
	}

	public void destroy() {
		protocol.destroy();
	}

}