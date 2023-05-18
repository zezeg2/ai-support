package io.github.zezeg2.aisupport.ai.function;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import lombok.Data;

@Data
public class Argument {
    private final WRAPPING wrapping;
    private final Class<?> type;
    private final String fieldName;
    private final Object value;
    private final String desc;

    public Argument(WRAPPING wrapping, Class<?> type, String fieldName, Object value) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = null;
    }

    public Argument(WRAPPING wrapping, Class<?> type, String fieldName, Object value, String desc) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
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
