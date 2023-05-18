package io.github.zezeg2.aisupport.ai.function.argument;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import lombok.Data;

@Data
public class SingleArgument<T> implements Argument<T> {
    private final Class<T> type;
    private final String fieldName;
    private final T value;
    private final String desc;

    public SingleArgument(Class<T> type, String fieldName, T value) {
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = null;
    }

    public SingleArgument(Class<T> type, String fieldName, T value, String desc) {
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Class<T> getWrapping() {
        return null;
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
