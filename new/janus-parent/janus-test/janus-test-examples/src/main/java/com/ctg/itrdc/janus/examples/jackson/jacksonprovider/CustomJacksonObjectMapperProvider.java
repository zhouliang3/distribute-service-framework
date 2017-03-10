package com.ctg.itrdc.janus.examples.jackson.jacksonprovider;

import com.ctg.itrdc.janus.common.json.JacksonObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dylan on 11/15/14.
 */
public class CustomJacksonObjectMapperProvider implements JacksonObjectMapperProvider {
    @Override
    public ObjectMapper getObjectMapper() {
        System.out.println("get object mapper from CustomJacksonObjectMapperProvider");
        return new ObjectMapper();
    }
}
