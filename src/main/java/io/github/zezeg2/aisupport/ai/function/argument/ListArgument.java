package io.github.zezeg2.aisupport.ai.function.argument;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import lombok.Data;

import java.util.List;

@Data
public class ListArgument<T> implements Argument<T> {
    private final WRAPPING wrapping;
    private final Class<T> type;
    private final String fieldName;
    private final List<T> value;
    private final String desc;

    public ListArgument(WRAPPING wrapping, Class<T> type, String fieldName, List<T> value) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = null;
    }

    public ListArgument(WRAPPING wrapping, Class<T> type, String fieldName, List<T> value, String desc) {
        this.wrapping = wrapping;
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String getTypeName() {
        return wrapping.toString() + "<" +type.getSimpleName() + ">";
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
