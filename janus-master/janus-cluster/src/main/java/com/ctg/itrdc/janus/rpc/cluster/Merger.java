package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * @author Administrator
 */
@SPI
public interface Merger<T> {

	T merge(T... items);

}
