package com.ctg.itrdc.janus.config.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数类型注解
 * 
 * @author Administrator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Parameter {

	String key() default "";

	boolean required() default false;

	boolean excluded() default false;

	boolean escaped() default false;

	boolean attribute() default false;

	boolean append() default false;

}