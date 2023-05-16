package io.github.zezeg2.aisupport.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BaseType {
    default Map<String, Object> getExampleMap() throws IllegalAccessException {
        Map<String, Object> fieldDescriptions = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);  // Allows us to access private fields

            FieldDesc fieldDesc = field.getAnnotation(FieldDesc.class);
            String description = fieldDesc != null ? fieldDesc.value() : field.getName();

            Object fieldValue = field.get(this);
            if (fieldValue == null) {
                if (BaseType.class.isAssignableFrom(field.getType())) {
                    try {
                        fieldValue = ((Class<?>) field.getType()).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create an instance of " + field.getType().getName(), e);
                    }
                } else if (List.class.isAssignableFrom(field.getType())) {
                    fieldValue = new ArrayList<>();
                    Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (BaseType.class.isAssignableFrom(listType)) {
                        try {
                            ((List<Object>) fieldValue).add(listType.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + listType.getName(), e);
                        }
                    }
                }
            }

            if (fieldValue instanceof BaseType) {
                Map<String, Object> nestedMap = ((BaseType) fieldValue).getExampleMap();
                fieldDescriptions.put(field.getName(), nestedMap);
            } else if (fieldValue instanceof List) {
                List<?> list = (List<?>) fieldValue;
                List<Object> listDescriptions = new ArrayList<>();
                for (Object listItem : list) {
                    if (listItem instanceof BaseType) {
                        listDescriptions.add(((BaseType) listItem).getExampleMap());
                    } else {
                        listDescriptions.add(listItem.toString());
                    }
                }
                fieldDescriptions.put(field.getName(), listDescriptions);
            } else {
                fieldDescriptions.put(field.getName(), description);
            }
        }

        return fieldDescriptions;
    }

    default String getExample() throws IllegalAccessException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(getExampleMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }
}
