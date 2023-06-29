package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JsonIgnore
public @interface FormatIgnore {
}
