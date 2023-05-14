package io.github.zezeg2.common.enums;

public enum PLANG {
    JAVA("JAVA"),
    TYPESCRIPT("TYPESCRIPT");

    private String value;

    PLANG(String value) {
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
