package io.github.zezeg2.aisupport.ai.function;

import lombok.Data;

@Data
public final class Argument {
    private final String fieldName;
    private final Object value;
    private final Class<?> type;
    private final String desc;

    public Argument(String fieldName, Object value, Class<?> type, String desc) {
        this.fieldName = fieldName;
        this.value = value;
        this.type = type;
        this.desc = desc;
    }

    public Argument(String fieldName, Object value, Class<?> type) {
        this.fieldName = fieldName;
        this.value = value;
        this.type = type;
        this.desc = null;
    }

    public String getTypeName() {
        return type.getSimpleName();
    }

    public String getValueToString() {
        return value.toString();
    }

    public String getFieldName() {
        return fieldName;
    }
}
