package io.github.zezeg2.aisupport.common.argument;

public class SingleArgument<T> extends BaseArgument<T> {
    public SingleArgument(Class<T> type, String fieldName, T value, String desc, Class<?> wrappedType) {
        super(type, fieldName, value, desc, wrappedType);
    }


    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Class<T> getWrapping() {
        return null;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public Object getValue() {
        return value;
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
