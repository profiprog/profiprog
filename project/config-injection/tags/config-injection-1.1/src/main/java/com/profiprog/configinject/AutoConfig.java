package com.profiprog.configinject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.profiprog.configinject.PropertyConverter.DefaultPopertyConverter;

@Target(value={ElementType.FIELD,ElementType.METHOD})
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented
public @interface AutoConfig {

	/**
	 * Expression of value.
	 */
	String value();
	
	/**
	 * Additional value for converter.
	 * etc. default value 
	 */
	String configValue() default "";
	
	Class<? extends PropertyConverter<?>> converter() default DefaultPopertyConverter.class;
	
	int priority() default 5;
}
