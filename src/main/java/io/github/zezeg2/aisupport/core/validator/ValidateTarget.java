package io.github.zezeg2.aisupport.core.validator;


import io.github.zezeg2.aisupport.config.properties.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a target for validation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValidateTarget {

    /**
     * Specifies if the validation is global.
     *
     * @return {@code true} if the validation is global, {@code false} otherwise.
     */
    boolean global() default false;

    /**
     * Specifies the names associated with the target for validation.
     *
     * @return An array of strings representing the names for the validation target.
     */
    String[] names() default {};

    /**
     * Specifies the order of validation.
     *
     * @return The order of validation as an integer value.
     */
    int order() default Integer.MAX_VALUE;

    /**
     * Specifies the topP value for the validation.
     *
     * @return The topP value as a double.
     */
    double topP() default 1d;

    /**
     * Specifies the model used for validation.
     *
     * @return The validation model as a {@link Model} enumeration.
     */
    Model model() default Model.NONE;
}
