package com.ctg.itrdc.janus.config.spring;

import com.ctg.itrdc.janus.rpc.service.GenericException;
import com.ctg.itrdc.janus.rpc.service.GenericService;

/**
 * @author <a href="mailto:gang.lvg@taobao.com">kimi</a>
 */
public class GenericDemoService implements GenericService {

    public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
        return null;
    }
}
