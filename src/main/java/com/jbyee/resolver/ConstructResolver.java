package com.jbyee.resolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConstructResolver {
    String resolve(Class<?> clazz);

    default void genClassMap(Class<?> clazz, Map<String, Map<String, List<String>>> classMap) {
        Map<String, List<String>> fieldsMap = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            List<String> fieldInfo = new ArrayList<>();
            fieldInfo.add(type.getSimpleName());

            if (!type.isPrimitive() && !String.class.equals(type) && !Number.class.isAssignableFrom(type)) {
                genClassMap(type, classMap);
            }

            fieldsMap.put(field.getName(), fieldInfo);
        }

        classMap.put(clazz.getSimpleName(), fieldsMap);
    }
}
