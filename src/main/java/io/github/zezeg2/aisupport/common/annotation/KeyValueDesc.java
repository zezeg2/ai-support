package io.github.zezeg2.aisupport.common.annotation;

import io.github.zezeg2.aisupport.common.type.Supportable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The KeyValueDesc annotation is used to provide descriptions for keys and values in a map field.
 * It can be applied to fields and accepts two optional properties: "key" and "value".
 *
 * @see Supportable
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface KeyValueDesc {

    /**
     * Specifies the description for the keys in the annotated map field.
     * This property is optional. If not specified, a default description will be used.
     *
     * @return The description for the keys in the map field.
     */
    String key() default "";

    /**
     * Specifies the description for the values in the annotated map field.
     * This property is optional. If not specified, a default description will be used.
     *
     * @return The description for the values in the map field.
     */
    String value() default "";
}