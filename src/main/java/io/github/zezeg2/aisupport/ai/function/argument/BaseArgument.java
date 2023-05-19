package io.github.zezeg2.aisupport.ai.function.argument;

public abstract class BaseArgument<T> implements Argument<T> {
    protected final Class<T> type;
    protected final String fieldName;
    protected final T value;
    protected final String desc;

    public BaseArgument(Class<T> type, String fieldName, T value, String desc) {
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
    }

}
