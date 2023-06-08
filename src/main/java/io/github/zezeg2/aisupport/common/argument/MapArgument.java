package io.github.zezeg2.aisupport.common.argument;

import java.util.Map;

public class MapArgument<T> extends BaseArgument<Map<String, T>> {

    public MapArgument(Class<Map<String, T>> type, String fieldName, Map<String, T> value, String desc, Class<?> wrappedType) {
        super(type, fieldName, value, desc, wrappedType);
    }

    @Override
    public Class<?> getType() {
        return wrappedType;
    }

    @Override
    public Class<Map<String, T>> getWrapping() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public Map<String, T> getValue() {
        return value;
    }

    @Override
    public String getTypeName() {
        return "Map<String, " + wrappedType.getSimpleName() + ">";
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
