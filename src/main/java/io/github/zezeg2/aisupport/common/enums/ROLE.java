package io.github.zezeg2.aisupport.common.enums;

public enum ROLE {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    ROLE(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
