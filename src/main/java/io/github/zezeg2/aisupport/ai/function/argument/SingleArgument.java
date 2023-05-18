package io.github.zezeg2.aisupport.ai.function.argument;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import lombok.Data;

@Data
public class SingleArgument<T> implements Argument<T> {
    private final WRAPPING wrapping;
    private final Class<T> type;
    private final String fieldName;
    private final Object value;
    private final String desc;

    public SingleArgument(WRAPPING wrapping, Class<T> type, String fieldName, Object value) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = null;
    }

    public SingleArgument(WRAPPING wrapping, Class<T> type, String fieldName, Object value, String desc) {
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
