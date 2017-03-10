package com.ctg.itrdc.janus.remoting.zookeeper.zkclient;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.remoting.zookeeper.ZookeeperClient;
import com.ctg.itrdc.janus.remoting.zookeeper.ZookeeperTransporter;

public class ZkclientZookeeperTransporter implements ZookeeperTransporter {

	public ZookeeperClient connect(URL url) {
		return new ZkclientZookeeperClient(url);
	}

}
