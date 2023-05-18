package io.github.zezeg2.aisupport.ai.function.argument;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import lombok.Data;

import java.util.Map;

@Data
public class MapArgument<T> implements Argument<T> {
    private final WRAPPING wrapping;
    private final Class<T> type;
    private final String fieldName;
    private final Map<String, T> value;
    private final String desc;

    public MapArgument(WRAPPING wrapping, Class<T> type, String fieldName, Map<String, T> value) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = null;
    }

    public MapArgument(WRAPPING wrapping, Class<T> type, String fieldName, Map<String, T> value, String desc) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String getTypeName() {
        return type.getSimpleName();
    }

    @Override
    public String getValueToString() {
        return value.toString();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }
}
