package io.github.zezeg2.aisupport.common.enums;

import java.util.Collection;
import java.util.Map;

public enum WRAPPING {
    NONE(Object.class),
    COLLECTION(Collection.class),
    MAP(Map.class);
    private Class<?> value;

    WRAPPING(Class<?> value) {
        this.value = value;
    }

    public Class<?> getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value != null ? this.value.getSimpleName() : "";
    }

}
