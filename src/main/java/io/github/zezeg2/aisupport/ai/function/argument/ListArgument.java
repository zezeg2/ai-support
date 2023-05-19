package io.github.zezeg2.aisupport.ai.function.argument;

import java.util.List;

public class ListArgument<T> extends BaseArgument<List<T>> {

    private final Class<T> wrappedType;

    public ListArgument(Class<List<T>> type, String fieldName, List<T> value, String desc, Class<T> wrappedType) {
        super(type, fieldName, value, desc);
        this.wrappedType = wrappedType;
    }

    @Override
    public Class<?> getType() {
        return wrappedType;
    }

    @Override
    public Class<List<T>> getWrapping() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public List<T> getValue() {
        return value;
    }

    @Override
    public String getTypeName() {
        return "List<" + type.getSimpleName() + ">";
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
