package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Supportable {
    @JsonIgnore
    default Map<String, Object> getFormatMap() throws IllegalAccessException {
        Map<String, Object> format = FormatStore.getFormat(this.getClass());
        if (format != null) return format;
        Map<String, Object> fieldDescriptions = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);  // Allows us to access private fields

            FieldDesc fieldDesc = field.getAnnotation(FieldDesc.class);
            String description = fieldDesc != null ? fieldDesc.value() : field.getName();

            Object fieldValue = field.get(this);
            Class<?> listType = null;
            if (fieldValue == null) {
                if (Supportable.class.isAssignableFrom(field.getType())) {
                    try {
                        fieldValue = ((Class<?>) field.getType()).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create an instance of " + field.getType().getName(), e);
                    }
                } else if (List.class.isAssignableFrom(field.getType())) {
                    fieldValue = new ArrayList<>();
                    listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (Supportable.class.isAssignableFrom(listType)) {
                        try {
                            ((List<Object>) fieldValue).add(listType.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + listType.getName(), e);
                        }
                    }
                }
            }

            if (fieldValue instanceof Supportable) {
                Map<String, Object> nestedMap = ((Supportable) fieldValue).getFormatMap();
                fieldDescriptions.put(field.getName(), nestedMap);
            } else if (fieldValue instanceof List) {
                List<?> list = (List<?>) fieldValue;
                List<Object> listDescriptions = new ArrayList<>();
                for (Object listItem : list) {
                    if (listItem instanceof Supportable) {
                        listDescriptions.add(((Supportable) listItem).getFormatMap());
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
        FormatStore.save(this.getClass(), fieldDescriptions);
        return fieldDescriptions;
    }

//    @JsonIgnore
//    default String getFormat() throws IllegalAccessException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getFormatMap());
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to convert map to JSON", e);
//        }
//    }
//
//    @JsonIgnore
//    default Object getFormatValue() throws IllegalAccessException {
//        return getFormatMap().entrySet().stream().findFirst().get().getValue();
//    }
}
