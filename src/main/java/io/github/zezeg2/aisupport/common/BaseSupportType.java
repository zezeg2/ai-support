package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BaseSupportType {
    @JsonIgnore
    default Map<String, Object> getExampleMap() throws IllegalAccessException {
        Map<String, Object> fieldDescriptions = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);  // Allows us to access private fields

            FieldDesc fieldDesc = field.getAnnotation(FieldDesc.class);
            String description = fieldDesc != null ? fieldDesc.value() : field.getName();

            Object fieldValue = field.get(this);
            Class<?> listType = null;
            if (fieldValue == null) {
                if (BaseSupportType.class.isAssignableFrom(field.getType())) {
                    try {
                        fieldValue = ((Class<?>) field.getType()).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create an instance of " + field.getType().getName(), e);
                    }
                } else if (List.class.isAssignableFrom(field.getType())) {
                    fieldValue = new ArrayList<>();
                    listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (BaseSupportType.class.isAssignableFrom(listType)) {
                        try {
                            ((List<Object>) fieldValue).add(listType.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + listType.getName(), e);
                        }
                    }
                }
            }

            if (fieldValue instanceof BaseSupportType) {
                Map<String, Object> nestedMap = ((BaseSupportType) fieldValue).getExampleMap();
                fieldDescriptions.put(field.getName(), nestedMap);
            } else if (fieldValue instanceof List) {
                List<?> list = (List<?>) fieldValue;
                List<Object> listDescriptions = new ArrayList<>();
                for (Object listItem : list) {
                    if (listItem instanceof BaseSupportType) {
                        listDescriptions.add(((BaseSupportType) listItem).getExampleMap());
                    } else {
                        listDescriptions.add(listItem.toString());
                    }
                }
                if (list.isEmpty() && listType != null && (String.class.isAssignableFrom(listType) || Number.class.isAssignableFrom(listType))) {
                    listDescriptions.add(description);
                }
                fieldDescriptions.put(field.getName(), listDescriptions);
            } else {
                fieldDescriptions.put(field.getName(), description);
            }
        }

        return fieldDescriptions;
    }

    @JsonIgnore
    default String getExample() throws IllegalAccessException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getExampleMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }

    @JsonIgnore
    default Object getExampleValue() throws IllegalAccessException {
        return getExampleMap().entrySet().stream().findFirst().get().getValue();
    }
}
