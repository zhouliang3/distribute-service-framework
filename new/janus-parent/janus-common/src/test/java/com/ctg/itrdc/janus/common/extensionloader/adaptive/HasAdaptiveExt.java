package com.ctg.itrdc.janus.common.extensionloader.adaptive;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.common.extension.Adaptive;
import com.ctg.itrdc.janus.common.extension.SPI;

/**
 * @author ding.lid
 */
@SPI
public interface HasAdaptiveExt {
    @Adaptive
    String echo(URL url, String s);
}
