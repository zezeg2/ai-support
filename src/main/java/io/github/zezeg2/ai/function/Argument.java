package io.github.zezeg2.ai.function;

public record Argument(String field, Object value, Class<?> type) {
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
