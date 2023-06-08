package io.github.zezeg2.aisupport.common.argument;

import java.util.List;

public class ListArgument<T> extends BaseArgument<List<T>> {

    public ListArgument(Class<List<T>> type, String fieldName, List<T> value, String desc, Class<?> wrappedType) {
        super(type, fieldName, value, desc, wrappedType);
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
        return "List<" + wrappedType.getSimpleName() + ">";
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
