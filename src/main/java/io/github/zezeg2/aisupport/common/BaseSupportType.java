package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseSupportType implements Supportable {
    @JsonIgnore
    public String getFormat() throws IllegalAccessException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getFormatMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }

    @JsonIgnore
    public Object getFormatValue() throws IllegalAccessException {
        return getFormatMap().entrySet().stream().findFirst().get().getValue();
    }
}
