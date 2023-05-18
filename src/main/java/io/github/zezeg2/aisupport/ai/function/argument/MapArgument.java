package io.github.zezeg2.aisupport.ai.function.argument;

import java.util.Map;

public class MapArgument<T> extends BaseArgument<Map<String, T>> {
    private final Class<T> wrappedType;

    public MapArgument(Class<Map<String, T>> type, String fieldName, Map<String, T> value, String desc, Class<T> wrappedType) {
        super(type, fieldName, value, desc);
        this.wrappedType = wrappedType;
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
        return "Map<String, " +wrappedType.getSimpleName() + ">";
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
