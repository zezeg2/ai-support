package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

/**
 * The BaseSupportType class provides a base implementation of the Supportable interface
 * and includes additional functionality for formatting and retrieving values.
 * It is an abstract class that implements Serializable.
 *
 * @since 1.0
 */
public abstract class BaseSupportType implements Supportable, Serializable {

    @FormatIgnore
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Retrieves a formatted JSON string representation of the implementing class.
     * It uses the ObjectMapper to serialize the format map to a JSON string,
     * using the default pretty printer for formatting.
     *
     * @return The formatted JSON string representation of the implementing class.
     * @throws RuntimeException If an exception occurs during the conversion process.
     */
    @JsonIgnore
    public String getFormat() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getFormatMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }

    /**
     * Retrieves the value of the first entry in the format map.
     * It returns the value associated with the first key in the format map.
     *
     * @return The value of the first entry in the format map.
     * @throws IllegalAccessException If there is an error accessing the format map.
     */
    @JsonIgnore
    public Object getFormatValue() throws IllegalAccessException {
        return getFormatMap().entrySet().stream().findFirst().get().getValue();
    }
}
