package com.jbyee.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public interface ConstructResolver {

    String toString(Map<String, Map<String, List<String>>> classMap);

    default String resolve(Class<?> clazz) {
        return toString(generateClassMap(Collections.singleton(clazz)));
    }

    default String resolve(Set<Class<?>> classSet) {
        return toString(generateClassMap(classSet));
    }

    default Map<String, Map<String, List<String>>> generateClassMap(Set<Class<?>> classSet) {
        Map<String, Map<String, List<String>>> classMap = new HashMap<>();

        for (Class<?> clazz : classSet) {
            if (clazz.equals(Object.class) ||
                    clazz.equals(String.class) ||
                    clazz.getSuperclass().equals(Number.class) ||
                    clazz.getSuperclass().equals(Collection.class) ||
                    clazz.getSuperclass().equals(Map.class) ||
                    clazz.getSuperclass().equals(HashMap.class) ||
                    clazz.getSuperclass().equals(Set.class) ||
                    clazz.getSuperclass().equals(HashSet.class) ||
                    clazz.getSuperclass().equals(List.class)
            ) continue;

            Map<String, List<String>> fieldsMap = new HashMap<>();
            for (Field field : clazz.getDeclaredFields()) {
                addFieldToMap(classMap, fieldsMap, field);
            }
            classMap.put(clazz.getSimpleName(), fieldsMap);
        }

        return classMap;
    }

    default void addFieldToMap(Map<String, Map<String, List<String>>> classMap, Map<String, List<String>> fieldsMap, Field field) {
        Class<?> type = field.getType();
        Type genericType = field.getGenericType();
        List<String> fieldInfo = new ArrayList<>();
        List<Class<?>> temp = new ArrayList<>();

        if (genericType instanceof ParameterizedType) {
            addParameterizedTypeToFieldInfo(fieldInfo, temp, (ParameterizedType) genericType, type);
        } else {
            fieldInfo.add(type.getSimpleName());
            type = (type.getComponentType() == null) ? type : type.getComponentType();
            temp.add(type);
        }

        analyzeNonPrimitiveClasses(classMap, temp);
        fieldsMap.put(field.getName(), fieldInfo);
    }

    default void addParameterizedTypeToFieldInfo(List<String> fieldInfo, List<Class<?>> temp, ParameterizedType parameterizedType, Class<?> type) {
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        String[] collect = Arrays.stream(typeArguments)
                .map(typeArgument -> {
                    if (typeArgument instanceof Class) {
                        temp.add(((Class<?>) typeArgument).getNestHost());
                        String[] split = typeArgument.getTypeName().split("\\.");
                        return split[split.length - 1];
                    } else {
                        // Handle the case where typeArgument is not a Class (could be a type variable, wildcard, etc.)
                        // You'll need to decide what to do in this case.
                        return typeArgument.getTypeName();
                    }
                })
                .toArray(String[]::new);

        fieldInfo.add(String.format("%s<%s>", type.getSimpleName(), String.join(", ", collect)));
    }


    default void analyzeNonPrimitiveClasses(Map<String, Map<String, List<String>>> classMap, List<Class<?>> classes) {
        classes.stream()
                .filter(t -> !t.isPrimitive() && !String.class.equals(t) && !Number.class.isAssignableFrom(t) && !Object.class.equals(t))
                .forEach(t -> generateClassMap(Collections.singleton(t)));
    }
}
