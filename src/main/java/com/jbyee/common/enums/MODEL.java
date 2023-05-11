package com.jbyee.common.enums;

public enum MODEL {
    SYSTEM("system"),
    USER("user");

    private String value;

    MODEL(String value) {
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
