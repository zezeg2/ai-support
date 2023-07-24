package io.github.zezeg2.aisupport.common.annotation;

import io.github.zezeg2.aisupport.common.type.Supportable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark fields that should be excluded from formatting when generating the format.
 * Only fields in classes implementing the Supportable interface with this annotation will be excluded.
 *
 * @version 1.0
 * @see Supportable
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FormatIgnore {
}