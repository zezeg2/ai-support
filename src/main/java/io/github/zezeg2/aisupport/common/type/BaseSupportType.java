package io.github.zezeg2.aisupport.common.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.annotation.FormatIgnore;

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
    public String getFormatString() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getFormatMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }

    @Override
    public String toString() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
