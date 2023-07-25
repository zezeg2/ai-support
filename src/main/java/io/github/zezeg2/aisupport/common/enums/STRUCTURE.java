package io.github.zezeg2.aisupport.common.enums;

import java.util.List;
import java.util.Map;

/**
 * The STRUCTURE enumeration classifies the type of objects based on their underlying class.
 */
public enum STRUCTURE {
    /**
     * The SINGLE structure represents a single object.
     */
    SINGLE(Object.class),

    /**
     * The LIST structure represents a list or collection of objects.
     */
    LIST(List.class),

    /**
     * The MAP structure represents a map or dictionary of key-value pairs.
     */
    MAP(Map.class);

    private final Class<?> value;

    /**
     * Constructor for the STRUCTURE enumeration.
     *
     * @param value The underlying class associated with the structure.
     */
    STRUCTURE(Class<?> value) {
        this.value = value;
    }

    /**
     * Retrieves the underlying class associated with the structure.
     *
     * @return The Class object representing the underlying class.
     */
    public Class<?> getValue() {
        return this.value;
    }

    /**
     * Returns the string representation of the underlying class (its simple name).
     *
     * @return The string representation of the underlying class.
     */
    @Override
    public String toString() {
        return this.value != null ? this.value.getSimpleName() : "";
    }
}
