package com.jbyee.ai;

public record ArgumentRecord(String filedName, Object value, Class<?> type) {
    public String getTypeField() {
        return type.getSimpleName() + " " + filedName;
    }

    public String getValue(){
        return value.toString();
    }

    public String getFiled(){
        return filedName;
    }
}
