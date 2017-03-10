package com.ctg.itrdc.janus.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.Version;
import com.ctg.itrdc.janus.common.extension.ExtensionLoader;
import com.ctg.itrdc.janus.common.utils.ConfigUtils;
import com.ctg.itrdc.janus.common.utils.NetUtils;
import com.ctg.itrdc.janus.common.utils.ReflectUtils;
import com.ctg.itrdc.janus.common.utils.StringUtils;
import com.ctg.itrdc.janus.common.utils.UrlUtils;
import com.ctg.itrdc.janus.config.support.Parameter;
import com.ctg.itrdc.janus.monitor.MonitorFactory;
import com.ctg.itrdc.janus.monitor.MonitorService;
import com.ctg.itrdc.janus.registry.RegistryFactory;
import com.ctg.itrdc.janus.registry.RegistryService;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.InvokerListener;
import com.ctg.itrdc.janus.rpc.ProxyFactory;
import com.ctg.itrdc.janus.rpc.cluster.Cluster;
import com.ctg.itrdc.janus.rpc.support.MockInvoker;

/**
 * 抽象接口类型的配置
 * 
 * @author Administrator
 * @export
 */
public abstract class AbstractInterfaceConfig extends AbstractMethodConfig {

	private static final long serialVersionUID = -1559314110797223229L;

	/**
	 * 服务接口的本地实现类名
	 */
	protected String local;

	/**
	 * 服务接口的本地实现类名
	 */
	protected String stub;

	/**
	 * 服务监控配置
	 */
	protected MonitorConfig monitor;

	/**
	 * 代理类型
	 */
	protected String proxy;

	/**
	 * 集群方式
	 */
	protected String cluster;

	/**
	 * 过滤器
	 */
	protected String filter;

	/**
	 * 监听器
	 */
	protected String listener;

	/**
	 * 负责人
	 */
	protected String owner;

	/**
	 * 连接数限制,0表示共享连接，否则为该服务独享连接数
	 */
	protected Integer connections;

	/**
	 * 连接数限制
	 */
	protected String layer;

	/**
	 * 应用信息
	 */
	protected ApplicationConfig application;

	/**
	 * 模块信息
	 */
	protected ModuleConfig module;

	/**
	 * 注册中心
	 */
	protected List<RegistryConfig> registries;

	/**
	 * callback实例个数限制
	 */
	private Integer callbacks;

	/**
	 * 连接事件
	 */
	protected String onconnect;

	/**
	 * 断开事件
	 */
	protected String ondisconnect;

	/**
	 * 服务暴露或引用的scope,如果为local，则表示只在当前JVM内查找.
	 */
	private String scope;

	protected void checkRegistry() {
		// 兼容旧版本
		if (registries == null || registries.size() == 0) {
			String address = ConfigUtils.getProperty("janus.registry.address");
			if (address != null && address.length() > 0) {
				registries = new ArrayList<RegistryConfig>();
				String[] as = address.split("\\s*[|]+\\s*");
				for (String a : as) {
					RegistryConfig registryConfig = new RegistryConfig();
					registryConfig.setAddress(a);
					registries.add(registryConfig);
				}
			}
		}
		if ((registries == null || registries.size() == 0)) {
			throw new IllegalStateException(
					(getClass().getSimpleName().startsWith("Reference") ? "No such any registry to refer service in consumer "
							: "No such any registry to export service in provider ")
							+ NetUtils.getLocalHost()
							+ " use janus version "
							+ Version.getVersion()
							+ ", Please add <janus.registry.address=\"...\" /> to your spring config. If you want unregister, please set <janus:service registry=\"N/A\" />");
		}
		for (RegistryConfig registryConfig : registries) {
			appendProperties(registryConfig);
		}
	}

	@SuppressWarnings("deprecation")
	protected void checkApplication() {
		// 兼容旧版本
		if (application == null) {
			String applicationName = ConfigUtils
					.getProperty("janus.application.name");
			if (applicationName != null && applicationName.length() > 0) {
				application = new ApplicationConfig();
			}
		}
		if (application == null) {
			throw new IllegalStateException(
					"No such application config! Please add <janus:application name=\"...\" /> to your spring config.");
		}
		appendProperties(application);

		String wait = ConfigUtils.getProperty(Constants.SHUTDOWN_WAIT_KEY);
		if (wait != null && wait.trim().length() > 0) {
			System.setProperty(Constants.SHUTDOWN_WAIT_KEY, wait.trim());
		} else {
			wait = ConfigUtils.getProperty(Constants.SHUTDOWN_WAIT_SECONDS_KEY);
			if (wait != null && wait.trim().length() > 0) {
				System.setProperty(Constants.SHUTDOWN_WAIT_SECONDS_KEY,
						wait.trim());
			}
		}
	}

	protected List<URL> loadRegistries(boolean provider) {
		checkRegistry();
		List<URL> registryList = new ArrayList<URL>();
		if (registries != null && registries.size() > 0) {
			for (RegistryConfig config : registries) {
				String address = config.getAddress();
				if (address == null || address.length() == 0) {
					address = Constants.ANYHOST_VALUE;
				}
				String sysaddress = System
						.getProperty("janus.registry.address");
				if (sysaddress != null && sysaddress.length() > 0) {
					address = sysaddress;
				}
				if (address != null
						&& address.length() > 0
						&& !RegistryConfig.NO_AVAILABLE
								.equalsIgnoreCase(address)) {
					Map<String, String> map = new HashMap<String, String>();
					appendParameters(map, application);
					appendParameters(map, config);
					map.put("path", RegistryService.class.getName());
					map.put("janus", Version.getVersion());
					map.put(Constants.TIMESTAMP_KEY,
							String.valueOf(System.currentTimeMillis()));
					if (ConfigUtils.getPid() > 0) {
						map.put(Constants.PID_KEY,
								String.valueOf(ConfigUtils.getPid()));
					}
					if (!map.containsKey("protocol")) {
						if (ExtensionLoader.getExtensionLoader(
								RegistryFactory.class).hasExtension("remote")) {
							map.put("protocol", "remote");
						} else {
							map.put("protocol", "janus");
						}
					}
					List<URL> urls = UrlUtils.parseURLs(address, map);
					for (URL url : urls) {
						url = url.addParameter(Constants.REGISTRY_KEY,
								url.getProtocol());
						url = url.setProtocol(Constants.REGISTRY_PROTOCOL);
						if ((provider && url.getParameter(
								Constants.REGISTER_KEY, true))
								|| (!provider && url.getParameter(
										Constants.SUBSCRIBE_KEY, true))) {
							registryList.add(url);
						}
					}
				}
			}
		}
		return registryList;
	}

	protected URL loadMonitor(URL registryURL) {
		if (monitor == null) {
			String monitorAddress = ConfigUtils
					.getProperty("janus.monitor.address");
			String monitorProtocol = ConfigUtils
					.getProperty("janus.monitor.protocol");
			if (monitorAddress != null && monitorAddress.length() > 0
					|| monitorProtocol != null && monitorProtocol.length() > 0) {
				monitor = new MonitorConfig();
			} else {
				return null;
			}
		}
		appendProperties(monitor);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constants.INTERFACE_KEY, MonitorService.class.getName());
		map.put("janus", Version.getVersion());
		map.put(Constants.TIMESTAMP_KEY,
				String.valueOf(System.currentTimeMillis()));
		if (ConfigUtils.getPid() > 0) {
			map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
		}
		appendParameters(map, monitor);
		String address = monitor.getAddress();
		String sysaddress = System.getProperty("janus.monitor.address");
		if (sysaddress != null && sysaddress.length() > 0) {
			address = sysaddress;
		}
		if (ConfigUtils.isNotEmpty(address)) {
			if (!map.containsKey(Constants.PROTOCOL_KEY)) {
				if (ExtensionLoader.getExtensionLoader(MonitorFactory.class)
						.hasExtension("logstat")) {
					map.put(Constants.PROTOCOL_KEY, "logstat");
				} else {
					map.put(Constants.PROTOCOL_KEY, "janus");
				}
			}
			return UrlUtils.parseURL(address, map);
		} else if (Constants.REGISTRY_PROTOCOL.equals(monitor.getProtocol())
				&& registryURL != null) {
			return registryURL
					.setProtocol("janus")
					.addParameter(Constants.PROTOCOL_KEY, "registry")
					.addParameterAndEncoded(Constants.REFER_KEY,
							StringUtils.toQueryString(map));
		}
		return null;
	}

	protected void checkInterfaceAndMethods(Class<?> interfaceClass,
			List<MethodConfig> methods) {
		// 接口不能为空
		if (interfaceClass == null) {
			throw new IllegalStateException("interface not allow null!");
		}
		// 检查接口类型必需为接口
		if (!interfaceClass.isInterface()) {
			throw new IllegalStateException("The interface class "
					+ interfaceClass + " is not a interface!");
		}
		// 检查方法是否在接口中存在
		if (methods != null && methods.size() > 0) {
			for (MethodConfig methodBean : methods) {
				String methodName = methodBean.getName();
				if (methodName == null || methodName.length() == 0) {
					throw new IllegalStateException(
							"<janus:method> name attribute is required! Please check: <janus:service interface=\""
									+ interfaceClass.getName()
									+ "\" ... ><janus:method name=\"\" ... /></<janus:reference>");
				}
				boolean hasMethod = false;
				for (java.lang.reflect.Method method : interfaceClass
						.getMethods()) {
					if (method.getName().equals(methodName)) {
						hasMethod = true;
						break;
					}
				}
				if (!hasMethod) {
					throw new IllegalStateException("The interface "
							+ interfaceClass.getName() + " not found method "
							+ methodName);
				}
			}
		}
	}

	protected void checkStubAndMock(Class<?> interfaceClass) {
		if (ConfigUtils.isNotEmpty(local)) {
			Class<?> localClass = ConfigUtils.isDefault(local) ? ReflectUtils
					.forName(interfaceClass.getName() + "Local") : ReflectUtils
					.forName(local);
			if (!interfaceClass.isAssignableFrom(localClass)) {
				throw new IllegalStateException("The local implemention class "
						+ localClass.getName() + " not implement interface "
						+ interfaceClass.getName());
			}
			try {
				ReflectUtils.findConstructor(localClass, interfaceClass);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("No such constructor \"public "
						+ localClass.getSimpleName() + "("
						+ interfaceClass.getName()
						+ ")\" in local implemention class "
						+ localClass.getName());
			}
		}
		if (ConfigUtils.isNotEmpty(stub)) {
			Class<?> localClass = ConfigUtils.isDefault(stub) ? ReflectUtils
					.forName(interfaceClass.getName() + "Stub") : ReflectUtils
					.forName(stub);
			if (!interfaceClass.isAssignableFrom(localClass)) {
				throw new IllegalStateException("The local implemention class "
						+ localClass.getName() + " not implement interface "
						+ interfaceClass.getName());
			}
			try {
				ReflectUtils.findConstructor(localClass, interfaceClass);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("No such constructor \"public "
						+ localClass.getSimpleName() + "("
						+ interfaceClass.getName()
						+ ")\" in local implemention class "
						+ localClass.getName());
			}
		}
		if (ConfigUtils.isNotEmpty(mock)) {
			if (mock.startsWith(Constants.RETURN_PREFIX)) {
				String value = mock.substring(Constants.RETURN_PREFIX.length());
				try {
					MockInvoker.parseMockValue(value);
				} catch (Exception e) {
					throw new IllegalStateException(
							"Illegal mock json value in <janus:service ... mock=\""
									+ mock + "\" />");
				}
			} else {
				Class<?> mockClass = ConfigUtils.isDefault(mock) ? ReflectUtils
						.forName(interfaceClass.getName() + "Mock")
						: ReflectUtils.forName(mock);
				if (!interfaceClass.isAssignableFrom(mockClass)) {
					throw new IllegalStateException(
							"The mock implemention class "
									+ mockClass.getName()
									+ " not implement interface "
									+ interfaceClass.getName());
				}
				try {
					mockClass.getConstructor(new Class<?>[0]);
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"No such empty constructor \"public "
									+ mockClass.getSimpleName()
									+ "()\" in mock implemention class "
									+ mockClass.getName());
				}
			}
		}
	}

	/**
	 * @deprecated Replace to <code>getStub()</code>
	 * @return local
	 */
	@Deprecated
	public String getLocal() {
		return local;
	}

	/**
	 * @deprecated Replace to <code>setStub(String)</code>
	 * @param local
	 */
	@Deprecated
	public void setLocal(String local) {
		checkName("local", local);
		this.local = local;
	}

	/**
	 * @deprecated Replace to <code>setStub(Boolean)</code>
	 * @param local
	 */
	@Deprecated
	public void setLocal(Boolean local) {
		if (local == null) {
			setLocal((String) null);
		} else {
			setLocal(String.valueOf(local));
		}
	}

	public String getStub() {
		return stub;
	}

	public void setStub(String stub) {
		checkName("stub", stub);
		this.stub = stub;
	}

	public void setStub(Boolean stub) {
		if (local == null) {
			setStub((String) null);
		} else {
			setStub(String.valueOf(stub));
		}
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		checkExtension(Cluster.class, "cluster", cluster);
		this.cluster = cluster;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		checkExtension(ProxyFactory.class, "proxy", proxy);
		this.proxy = proxy;
	}

	public Integer getConnections() {
		return connections;
	}

	public void setConnections(Integer connections) {
		this.connections = connections;
	}

	@Parameter(key = Constants.REFERENCE_FILTER_KEY, append = true)
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		checkMultiExtension(Filter.class, "filter", filter);
		this.filter = filter;
	}

	@Parameter(key = Constants.INVOKER_LISTENER_KEY, append = true)
	public String getListener() {
		checkMultiExtension(InvokerListener.class, "listener", listener);
		return listener;
	}

	public void setListener(String listener) {
		this.listener = listener;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		checkNameHasSymbol("layer", layer);
		this.layer = layer;
	}

	public ApplicationConfig getApplication() {
		return application;
	}

	public void setApplication(ApplicationConfig application) {
		this.application = application;
	}

	public ModuleConfig getModule() {
		return module;
	}

	public void setModule(ModuleConfig module) {
		this.module = module;
	}

	public RegistryConfig getRegistry() {
		return registries == null || registries.size() == 0 ? null : registries
				.get(0);
	}

	public void setRegistry(RegistryConfig registry) {
		List<RegistryConfig> registries = new ArrayList<RegistryConfig>(1);
		registries.add(registry);
		this.registries = registries;
	}

	public List<RegistryConfig> getRegistries() {
		return registries;
	}

	@SuppressWarnings({ "unchecked" })
	public void setRegistries(List<? extends RegistryConfig> registries) {
		this.registries = (List<RegistryConfig>) registries;
	}

	public MonitorConfig getMonitor() {
		return monitor;
	}

	public void setMonitor(MonitorConfig monitor) {
		this.monitor = monitor;
	}

	public void setMonitor(String monitor) {
		this.monitor = new MonitorConfig(monitor);
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		checkMultiName("owner", owner);
		this.owner = owner;
	}

	public void setCallbacks(Integer callbacks) {
		this.callbacks = callbacks;
	}

	public Integer getCallbacks() {
		return callbacks;
	}

	public String getOnconnect() {
		return onconnect;
	}

	public void setOnconnect(String onconnect) {
		this.onconnect = onconnect;
	}

	public String getOndisconnect() {
		return ondisconnect;
	}

	public void setOndisconnect(String ondisconnect) {
		this.ondisconnect = ondisconnect;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}