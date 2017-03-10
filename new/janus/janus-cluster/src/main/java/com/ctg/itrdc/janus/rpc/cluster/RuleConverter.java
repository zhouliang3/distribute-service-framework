package com.ctg.itrdc.janus.rpc.cluster;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.SPI;

import java.util.List;

/**
 * 规则转换器
 * 
 * @author Administrator
 */
@SPI
public interface RuleConverter {

	List<URL> convert(URL subscribeUrl, Object source);

}
