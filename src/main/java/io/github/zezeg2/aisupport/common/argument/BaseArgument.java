package io.github.zezeg2.aisupport.common.argument;

public abstract class BaseArgument<T> implements Argument<T> {
    protected final Class<T> type;
    protected final String fieldName;
    protected final T value;
    protected final String desc;
    protected final Class<?> wrappedType;

    public BaseArgument(Class<T> type, String fieldName, T value, String desc, Class<?> wrappedType) {
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
        this.wrappedType = wrappedType;
    }

}
