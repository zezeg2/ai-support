package io.github.zezeg2.aisupport.common.enums;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDesc {
    String value();
}
