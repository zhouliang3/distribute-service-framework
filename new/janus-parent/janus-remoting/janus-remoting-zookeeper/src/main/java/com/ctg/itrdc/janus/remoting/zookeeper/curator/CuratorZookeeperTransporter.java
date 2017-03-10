package com.ctg.itrdc.janus.remoting.zookeeper.curator;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.zookeeper.ZookeeperClient;
import com.ctg.itrdc.janus.remoting.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

	public ZookeeperClient connect(URL url) {
		return new CuratorZookeeperClient(url);
	}

}
