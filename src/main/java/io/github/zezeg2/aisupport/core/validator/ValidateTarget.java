package io.github.zezeg2.aisupport.core.validator;


import io.github.zezeg2.aisupport.config.properties.MODEL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValidateTarget {
    boolean global() default false;

    String[] names() default {};

    int order() default Integer.MAX_VALUE;

    MODEL model() default MODEL.NONE;
}
