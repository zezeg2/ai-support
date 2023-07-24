package io.github.zezeg2.aisupport.common.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.zezeg2.aisupport.common.annotation.FieldDesc;
import io.github.zezeg2.aisupport.common.annotation.FormatIgnore;
import io.github.zezeg2.aisupport.common.annotation.KeyValueDesc;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * The Supportable interface provides functionality for generating formatted descriptions
 * of classes implementing this interface. It defines methods for extracting field descriptions
 * and converting them to a map structure.
 *
 * @since 1.0
 */
public interface Supportable {
    /**
     * Retrieves a formatted map representation of the implementing class.
     * The map contains descriptions of the fields in the class and their corresponding values.
     *
     * @return The formatted map representation of the implementing class.
     */
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

        SupportableFormatRegistry.save(Objects.requireNonNull(this.getClass()), fieldDescriptions);
        return fieldDescriptions;
    }

    /**
     * Handles a field and its corresponding description in the class.
     * It checks if the field is annotated with FieldDesc and retrieves the description from the annotation.
     * The field value is then examined and processed accordingly:
     * - If it is an instance of Supportable, the field is added to the fieldDescriptions map as a nested map.
     * - If it is a List, the field is added to the fieldDescriptions map as a list of descriptions.
     * - If it is a Map, the field is added to the fieldDescriptions map as a map of descriptions.
     * - Otherwise, the field is added to the fieldDescriptions map with its description as the value.
     *
     * @param field             The field to be handled.
     * @param fieldDescriptions The map of field descriptions.
     */
    default void handleField(Field field, Map<String, Object> fieldDescriptions) {
        FieldDesc fieldDesc = field.getAnnotation(FieldDesc.class);
        String description = fieldDesc != null ? fieldDesc.value() : field.getName();
        Object fieldValue = getFieldValue(field);

        if (fieldValue instanceof Supportable) {
            Map<String, Object> nestedMap = ((Supportable) fieldValue).getFormatMap();
            fieldDescriptions.put(field.getName(), nestedMap);
        } else if (fieldValue instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> listDescriptions = getListDescription((List<Object>) fieldValue, description);
            fieldDescriptions.put(field.getName(), listDescriptions);
        } else if (fieldValue instanceof Map<?, ?>) {
            Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            KeyValueDesc keyValueDesc = field.getAnnotation(KeyValueDesc.class);
            String mapKeyDescription = keyValueDesc == null || keyValueDesc.key().equals("") ? getSimpleTypeName(actualTypeArguments[0]) + " Key" : keyValueDesc.key();
            String mapValueDescription = keyValueDesc == null || keyValueDesc.value().equals("") ? getSimpleTypeName(actualTypeArguments[1]) + " Value" : keyValueDesc.value();
            @SuppressWarnings("unchecked")
            Map<String, Object> mapDescription = getMapDescription(field.getName(), (Map<String, Object>) fieldValue, mapKeyDescription, mapValueDescription);
            fieldDescriptions.put(field.getName(), mapDescription.get(field.getName()));
        } else {
            fieldDescriptions.put(field.getName(), description);
        }
    }

    /**
     * Retrieves a simplified name for a given type.
     * It extracts the simple name from the fully qualified type name.
     *
     * @param type The type whose simple name is to be retrieved.
     * @return The simplified name of the type.
     */
    default String getSimpleTypeName(Type type) {
        String[] split = type.getTypeName().split("\\.");
        return split[split.length - 1];
    }

    /**
     * Retrieves the value of a field by accessing it using reflection.
     *
     * @param field The field to retrieve the value from.
     * @return The value of the field.
     * @throws RuntimeException If there is an error accessing the field.
     */
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

    /**
     * Creates a new instance of a Supportable class using its default constructor.
     *
     * @param clazz The Supportable class to instantiate.
     * @return A new instance of the Supportable class.
     * @throws RuntimeException If there is an error creating the instance.
     */
    default Object instantiateSupportable(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of " + clazz.getName(), e);
        }
    }

    /**
     * Converts a list to a list of descriptions.
     * If the list contains Supportable objects, each object is converted to its corresponding format map.
     * Otherwise, the objects are converted to their string representations.
     * If the list is empty, a single description is added to represent the list.
     *
     * @param list        The list to be converted.
     * @param description The description of the list.
     * @return A list of descriptions for the objects in the original list.
     */
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

    /**
     * Converts a map to a map of descriptions.
     * If the map contains Supportable values, each value is converted to its corresponding format map.
     * If the map is empty, a single description map is added to represent the map.
     *
     * @param fieldName           The name of the field corresponding to the map.
     * @param map                 The map to be converted.
     * @param mapKeyDescription   The description of the map keys.
     * @param mapValueDescription The description of the map values.
     * @return A map of descriptions for the key-value pairs in the original map.
     */
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
