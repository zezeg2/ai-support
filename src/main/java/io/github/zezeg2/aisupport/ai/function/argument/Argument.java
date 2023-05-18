package io.github.zezeg2.aisupport.ai.function.argument;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;

public interface Argument<T> {

    Class<T> getType();

    String getDesc();

    WRAPPING getWrapping();

    Object getValue();

    String getTypeName();

    String getValueToString();

    String getFieldName();
}
