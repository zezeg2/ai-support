package io.github.zezeg2.aisupport.common.argument;

public interface Argument<T> {

    Class<?> getType();

    Class<T> getWrapping();

    String getDesc();

    Object getValue();

    String getTypeName();

    String getValueToString();

    String getFieldName();


}
