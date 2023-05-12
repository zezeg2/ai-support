package com.jbyee.ai;

public record ArgumentRecord(String field, Object value, Class<?> type) {
    public String getType() {
        return type.getSimpleName();
    }

    public String getValue() {
        return value.toString();
    }

    public String getField() {
        return field;
    }
}
