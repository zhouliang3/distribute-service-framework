package com.ctg.itrdc.janus.remoting.zookeeper;

import java.util.List;

public interface ChildListener {

	void childChanged(String path, List<String> children);

}