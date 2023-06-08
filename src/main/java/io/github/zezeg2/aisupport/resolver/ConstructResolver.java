package io.github.zezeg2.aisupport.resolver;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public interface ConstructResolver {

    String toString(Map<String, Map<String, List<String>>> classMap);

    default String resolve(Class<?> clazz) {
        return toString(generateClassMap(Collections.singleton(clazz), null));
    }

    default String resolve(Set<Class<?>> classSet) {
        return toString(generateClassMap(classSet, null));
    }

    default Map<String, Map<String, List<String>>> generateClassMap(Set<Class<?>> classSet, Map<String, Map<String, List<String>>> classMap) {
        if (classMap == null) classMap = new HashMap<>();

        for (Class<?> clazz : classSet) {
            if (clazz.getSuperclass() == null) continue;
            if (clazz.equals(Object.class) ||
                    clazz.equals(String.class) ||
                    clazz.getSuperclass().equals(Number.class) ||
                    clazz.getSuperclass().equals(Collection.class) ||
                    clazz.getSuperclass().equals(Map.class) ||
                    clazz.getSuperclass().equals(HashMap.class) ||
                    clazz.getSuperclass().equals(Set.class) ||
                    clazz.getSuperclass().equals(HashSet.class) ||
                    clazz.getSuperclass().equals(List.class) ||
                    clazz.getSuperclass().equals(Mono.class) ||
                    clazz.getSuperclass().equals(Flux.class)
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
        List<String> collect = new ArrayList<>();

        for (Type typeArgument : typeArguments) {
            if (typeArgument instanceof Class) {
                temp.add(((Class<?>) typeArgument).getNestHost());
                String[] split = typeArgument.getTypeName().split("\\.");
                collect.add(split[split.length - 1]);
            } else if (typeArgument instanceof ParameterizedType) {
                // Recursive call to handle nested generic types
                List<String> nestedFieldInfo = new ArrayList<>();
                addParameterizedTypeToFieldInfo(nestedFieldInfo, temp, (ParameterizedType) typeArgument, (Class<?>) ((ParameterizedType) typeArgument).getRawType());
                collect.add(nestedFieldInfo.get(0));
            } else {
                // Handle the case where typeArgument is not a Class (could be a type variable, wildcard, etc.)
                collect.add(typeArgument.getTypeName());
            }
        }

        fieldInfo.add(String.format("%s<%s>", type.getSimpleName(), String.join(", ", collect)));
    }


    default void analyzeNonPrimitiveClasses(Map<String, Map<String, List<String>>> classMap, List<Class<?>> classes) {
        classes.stream()
                .filter(t -> !t.isPrimitive() && !String.class.equals(t) && !Number.class.isAssignableFrom(t) && !Object.class.equals(t))
                .forEach(t -> generateClassMap(Collections.singleton(t), classMap));
    }
}
