package io.github.zezeg2.aisupport.common.enums;

import java.util.List;
import java.util.Map;

public enum STRUCTURE {
    SINGLE(Object.class),
    LIST(List.class),
    MAP(Map.class);
    private final Class<?> value;

    STRUCTURE(Class<?> value) {
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
