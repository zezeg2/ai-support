package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseSupportType implements Supportable {
    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonIgnore
    public String getFormat() {

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getFormatMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }

    @JsonIgnore
    public Object getFormatValue() throws IllegalAccessException {
        return getFormatMap().entrySet().stream().findFirst().get().getValue();
    }
}
