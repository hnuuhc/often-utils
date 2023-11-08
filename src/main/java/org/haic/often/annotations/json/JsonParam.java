package org.haic.often.annotations.json;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonParam {

	@AliasFor("name") String value() default "";

	@AliasFor("value") String name() default "";

	String[] exist() default {};

	boolean required() default true;

	String defaultValue() default ValueConstants.DEFAULT_NONE;
}
