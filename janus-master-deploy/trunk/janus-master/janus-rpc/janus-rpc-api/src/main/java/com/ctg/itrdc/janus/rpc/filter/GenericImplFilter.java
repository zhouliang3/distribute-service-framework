/*
 * Copyright 2016-2017 CHINA TELECOM GROUP.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ctg.itrdc.janus.rpc.filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.ctg.itrdc.janus.common.Constants;
import com.ctg.itrdc.janus.common.beanutil.JavaBeanAccessor;
import com.ctg.itrdc.janus.common.beanutil.JavaBeanDescriptor;
import com.ctg.itrdc.janus.common.beanutil.JavaBeanSerializeUtil;
import com.ctg.itrdc.janus.common.extension.Activate;
import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import com.ctg.itrdc.janus.common.utils.PojoUtils;
import com.ctg.itrdc.janus.common.utils.ReflectUtils;
import com.ctg.itrdc.janus.rpc.Filter;
import com.ctg.itrdc.janus.rpc.Invocation;
import com.ctg.itrdc.janus.rpc.Invoker;
import com.ctg.itrdc.janus.rpc.Result;
import com.ctg.itrdc.janus.rpc.RpcException;
import com.ctg.itrdc.janus.rpc.RpcInvocation;
import com.ctg.itrdc.janus.rpc.RpcResult;
import com.ctg.itrdc.janus.rpc.service.GenericException;
import com.ctg.itrdc.janus.rpc.support.ProtocolUtils;

/**
 * GenericImplInvokerFilter
 * 
 *
 */
@Activate(group = Constants.CONSUMER, value = Constants.GENERIC_KEY, order = 20000)
public class GenericImplFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericImplFilter.class);

    private static final Class<?>[] GENERIC_PARAMETER_TYPES = new Class<?>[] {String.class, String[].class, Object[].class};

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String generic = invoker.getUrl().getParameter(Constants.GENERIC_KEY);
        if (ProtocolUtils.isGeneric(generic)
                && ! Constants.$INVOKE.equals(invocation.getMethodName())
                && invocation instanceof RpcInvocation) {
            RpcInvocation invocation2 = (RpcInvocation) invocation;
            String methodName = invocation2.getMethodName();
            Class<?>[] parameterTypes = invocation2.getParameterTypes();
            Object[] arguments = invocation2.getArguments();
            
            String[] types = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i ++) {
                types[i] = ReflectUtils.getName(parameterTypes[i]);
            }

            Object[] args;
            if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                args = new Object[arguments.length];
                for(int i = 0; i < arguments.length; i++) {
                    args[i] = JavaBeanSerializeUtil.serialize(arguments[i], JavaBeanAccessor.METHOD);
                }
            } else {
                args = PojoUtils.generalize(arguments);
            }
            
            invocation2.setMethodName(Constants.$INVOKE);
            invocation2.setParameterTypes(GENERIC_PARAMETER_TYPES);
            invocation2.setArguments(new Object[] {methodName, types, args});
            Result result = invoker.invoke(invocation2);
            
            if (! result.hasException()) {
                Object value = result.getValue();
                try {
                    Method method = invoker.getInterface().getMethod(methodName, parameterTypes);
                    if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                        if (value == null) {
                            return new RpcResult(value);
                        } else if (value instanceof JavaBeanDescriptor) {
                            return new RpcResult(JavaBeanSerializeUtil.deserialize((JavaBeanDescriptor)value));
                        } else {
                            throw new RpcException(
                                new StringBuilder(64)
                                    .append("The type of result value is ")
                                    .append(value.getClass().getName())
                                    .append(" other than ")
                                    .append(JavaBeanDescriptor.class.getName())
                                    .append(", and the result is ")
                                    .append(value).toString());
                        }
                    } else {
                        return new RpcResult(PojoUtils.realize(value, method.getReturnType(), method.getGenericReturnType()));
                    }
                } catch (NoSuchMethodException e) {
                    throw new RpcException(e.getMessage(), e);
                }
            } else if (result.getException() instanceof GenericException) {
                GenericException exception = (GenericException) result.getException();
                try {
                    String className = exception.getExceptionClass();
                    Class<?> clazz = ReflectUtils.forName(className);
                    Throwable targetException = null;
                    Throwable lastException = null;
                    try {
                        targetException = (Throwable) clazz.newInstance();
                    } catch (Throwable e) {
                        lastException = e;
                        for (Constructor<?> constructor : clazz.getConstructors()) {
                            try {
                                targetException = (Throwable) constructor.newInstance(new Object[constructor.getParameterTypes().length]);
                                break;
                            } catch (Throwable e1) {
                                lastException = e1;
                            }
                        }
                    }
                    if (targetException != null) {
                        try {
                            Field field = Throwable.class.getDeclaredField("detailMessage");
                            if (! field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(targetException, exception.getExceptionMessage());
                        } catch (Throwable e) {
                            logger.warn(e.getMessage(), e);
                        }
                        result = new RpcResult(targetException);
                    } else if (lastException != null) {
                        throw lastException;
                    }
                } catch (Throwable e) {
                    throw new RpcException("Can not deserialize exception " + exception.getExceptionClass() + ", message: " + exception.getExceptionMessage(), e);
                }
            }
            return result;
        }

        if (invocation.getMethodName().equals(Constants.$INVOKE)
            && invocation.getArguments() != null
            && invocation.getArguments().length == 3
            && ProtocolUtils.isGeneric(generic)) {

            Object[] args = (Object[]) invocation.getArguments()[2];
            if (ProtocolUtils.isJavaGenericSerialization(generic)) {

                for (Object arg : args) {
                    if (!(byte[].class == arg.getClass())) {
                        error(byte[].class.getName(), arg.getClass().getName());
                    }
                }
            } else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                for(Object arg : args) {
                    if (!(arg instanceof JavaBeanDescriptor)) {
                        error(JavaBeanDescriptor.class.getName(), arg.getClass().getName());
                    }
                }
            }

            ((RpcInvocation)invocation).setAttachment(
                Constants.GENERIC_KEY, invoker.getUrl().getParameter(Constants.GENERIC_KEY));
        }
        return invoker.invoke(invocation);
    }

    private void error(String expected, String actual) throws RpcException {
        throw new RpcException(
            new StringBuilder(32)
                .append("Generic serialization [")
                .append(Constants.GENERIC_SERIALIZATION_NATIVE_JAVA)
                .append("] only support message type ")
                .append(expected)
                .append(" and your message type is ")
                .append(actual).toString());
    }

}