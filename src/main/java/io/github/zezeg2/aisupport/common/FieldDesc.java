package io.github.zezeg2.aisupport.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The FieldDesc annotation is used to provide a description for a field.
 * It can be applied to fields and accepts a single property "value" to specify the description.
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldDesc {

    /**
     * Specifies the description for the annotated field.
     *
     * @return The description for the field.
     */
    String value();
}
