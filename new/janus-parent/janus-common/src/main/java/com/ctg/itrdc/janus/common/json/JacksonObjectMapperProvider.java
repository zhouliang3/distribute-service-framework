package com.ctg.itrdc.janus.common.json;

import com.ctg.itrdc.janus.common.extension.SPI;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dylan on 11/12/14.
 */
@SPI("jackson")
public interface JacksonObjectMapperProvider {
    public ObjectMapper getObjectMapper();
}
