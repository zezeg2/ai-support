package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public interface Supportable {
    @JsonIgnore
    default Map<String, Object> getFormatMap() {
        Map<String, Object> format = SupportableFormatRegistry.getFormat(this.getClass());
        if (format != null) {
            return format;
        }

        Map<String, Object> fieldDescriptions = new HashMap<>();
        Class<?> currentClass = this.getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.getAnnotation(FormatIgnore.class) != null) continue;
                field.setAccessible(true);
                handleField(field, fieldDescriptions);
            }
            currentClass = currentClass.getSuperclass();
        }

        SupportableFormatRegistry.save(this.getClass(), fieldDescriptions);
        return fieldDescriptions;
    }


    default void handleField(Field field, Map<String, Object> fieldDescriptions) {
        FieldDesc fieldDesc = field.getAnnotation(FieldDesc.class);
        String description = fieldDesc != null ? fieldDesc.value() : field.getName();
        Object fieldValue = getFieldValue(field);

        if (fieldValue instanceof Supportable) {
            Map<String, Object> nestedMap = ((Supportable) fieldValue).getFormatMap();
            fieldDescriptions.put(field.getName(), nestedMap);
        } else if (fieldValue instanceof List<?>) {
            List<Object> listDescriptions = getListDescription((List<Object>) fieldValue, description);
            fieldDescriptions.put(field.getName(), listDescriptions);
        } else if (fieldValue instanceof Map<?, ?>) {
            Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            MapFieldDesc mapFieldDesc = field.getAnnotation(MapFieldDesc.class);
            String mapKeyDescription = mapFieldDesc == null || mapFieldDesc.key().equals("") ? actualTypeArguments[0] + " Key" : mapFieldDesc.key();
            String mapValueDescription = mapFieldDesc == null || mapFieldDesc.value().equals("") ? actualTypeArguments[1] + " Value" : mapFieldDesc.value();
            Map<String, Object> mapDescription = getMapDescription(field.getName(), (Map<String, Object>) fieldValue, mapKeyDescription, mapValueDescription);
            fieldDescriptions.put(field.getName(), mapDescription);
        } else {
            fieldDescriptions.put(field.getName(), description);
        }
    }

    default Object getFieldValue(Field field) {
        Object fieldValue;
        try {
            fieldValue = field.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field", e);
        }
        Class<?> actualType;
        if (fieldValue == null) {
            Class<?> fieldType = field.getType();
            if (Supportable.class.isAssignableFrom(fieldType)) fieldValue = instantiateSupportable(fieldType);
            else if (List.class.isAssignableFrom(fieldType)) {
                fieldValue = new ArrayList<>();
                actualType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (Supportable.class.isAssignableFrom(actualType))
                    ((List<Object>) fieldValue).add(instantiateSupportable(actualType));
            } else if (Map.class.isAssignableFrom(fieldType)) {
                fieldValue = new HashMap<>();
                Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                actualType = (Class<?>) actualTypeArguments[1];
                if (Supportable.class.isAssignableFrom(actualType)) {
                    ((Map<String, Object>) fieldValue).put(field.getName(), instantiateSupportable(actualType));
                }
            }
        }
        return fieldValue;
    }

    default Object instantiateSupportable(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of " + clazz.getName(), e);
        }
    }

    default List<Object> getListDescription(List<Object> list, String description) {
        List<Object> listDescriptions = new ArrayList<>();
        for (Object listItem : list) {
            if (listItem instanceof Supportable) {
                listDescriptions.add(((Supportable) listItem).getFormatMap());
            } else {
                listDescriptions.add(listItem.toString());
            }
        }
        if (list.isEmpty()) listDescriptions.add(description);
        return listDescriptions;
    }

    default Map<String, Object> getMapDescription(String fieldName, Map<String, Object> map, String mapKeyDescription, String mapValueDescription) {
        Map<String, Object> mapDescription = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Supportable)
                mapDescription.put(mapKeyDescription, ((Supportable) entry.getValue()).getFormatMap());
        }
        if (map.isEmpty()) mapDescription.put(fieldName, Map.of(mapKeyDescription, mapValueDescription));
        return mapDescription;
    }
}
