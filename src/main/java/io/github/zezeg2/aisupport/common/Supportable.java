package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public interface Supportable {
    @JsonIgnore
    default Map<String, Object> getFormatMap() throws IllegalAccessException {
        Map<String, Object> format = SupportableFormatStore.getFormat(this.getClass());
        if (format != null) return format;
        Map<String, Object> fieldDescriptions = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);  // Allows us to access private fields

            FieldDesc fieldDesc = field.getAnnotation(FieldDesc.class);
            MapFieldDesc mapFieldDesc = field.getAnnotation(MapFieldDesc.class);
            String description = fieldDesc != null ? fieldDesc.value() : field.getName();
            String mapKeyDescription = mapFieldDesc != null && !mapFieldDesc.key().isEmpty() ? mapFieldDesc.key() : null;
            String mapValueDescription = mapFieldDesc != null && !mapFieldDesc.value().isEmpty() ? mapFieldDesc.value() : null;


            Object fieldValue = field.get(this);
            Class<?> actualType = null;
            if (fieldValue == null) {
                if (Supportable.class.isAssignableFrom(field.getType())) {
                    try {
                        fieldValue = ((Class<?>) field.getType()).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create an instance of " + field.getType().getName(), e);
                    }
                } else if (List.class.isAssignableFrom(field.getType())) {
                    fieldValue = new ArrayList<>();
                    actualType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (Supportable.class.isAssignableFrom(actualType)) {
                        try {
                            ((List<Object>) fieldValue).add(actualType.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + actualType.getName(), e);
                        }
                    }
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    fieldValue = new HashMap<>();
                    Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    actualType = (Class<?>) actualTypeArguments[1];
                    mapKeyDescription = mapKeyDescription == null ? actualTypeArguments[0] + " Key" : mapKeyDescription;
                    mapValueDescription = mapValueDescription == null ? actualTypeArguments[1] + " Value" : mapValueDescription;
                    if (Supportable.class.isAssignableFrom(actualType)) {
                        try {
                            ((Map<String, Object>) fieldValue).put(field.getName(), actualType.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + actualType.getName(), e);
                        }
                    }
                }
            }

            if (fieldValue instanceof Supportable) {
                Map<String, Object> nestedMap = ((Supportable) fieldValue).getFormatMap();
                fieldDescriptions.put(field.getName(), nestedMap);
            } else if (fieldValue instanceof List<?> list) {
                List<Object> listDescriptions = new ArrayList<>();
                for (Object listItem : list) {
                    if (listItem instanceof Supportable) {
                        listDescriptions.add(((Supportable) listItem).getFormatMap());
                    } else {
                        listDescriptions.add(listItem.toString());
                    }
                }
                if (list.isEmpty() && actualType != null && (String.class.isAssignableFrom(actualType) || Number.class.isAssignableFrom(actualType))) {
                    listDescriptions.add(description);
                }
                fieldDescriptions.put(field.getName(), listDescriptions);
            } else if (fieldValue instanceof Map<?, ?> map) {
                Map<String, Object> mapDescription = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
                    if (entry.getValue() instanceof Supportable) {
                        mapDescription.put(mapKeyDescription, ((Supportable) entry.getValue()).getFormatMap());
                    } else {
                        mapDescription.put(mapKeyDescription, Map.of(mapKeyDescription, mapValueDescription));
                    }
                }
                if (map.isEmpty() && actualType != null && (String.class.isAssignableFrom(actualType) || Number.class.isAssignableFrom(actualType))) {
                    mapDescription.put(mapKeyDescription, mapValueDescription);
                }
                fieldDescriptions.put(field.getName(), mapDescription);
            } else {
                fieldDescriptions.put(field.getName(), description);
            }
        }
        SupportableFormatStore.save(this.getClass(), fieldDescriptions);
        return fieldDescriptions;
    }
}
