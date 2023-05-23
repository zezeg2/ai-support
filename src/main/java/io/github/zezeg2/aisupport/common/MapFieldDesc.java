package io.github.zezeg2.aisupport.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MapFieldDesc {
    String key() default "";

    String value() default "";
}
