package io.github.zezeg2.aisupport.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static Map<Integer, List<Class<?>>> getParameterTypesMap(Class<?> clazz) {
        Type type = clazz.getGenericSuperclass();
        Map<Integer, List<Class<?>>> map = new HashMap<>();
        exploreTypes(type, map, 1);
        return map;
    }

    public static Map<Integer, List<String>> getParameterTypesSimpleNameMap(Class<?> clazz) {
        return getParameterTypesMap(clazz).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(Class::getSimpleName).toList(), (a, b) -> b));
    }

    private static void exploreTypes(Type type, Map<Integer, List<Class<?>>> map, int depth) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            List<Class<?>> typeParameters = map.computeIfAbsent(depth, k -> new ArrayList<>());
            typeParameters.add(rawType);

            for (Type actualType : parameterizedType.getActualTypeArguments()) {
                if (actualType instanceof Class) {
                    Class<?> actualClass = (Class<?>) actualType;
                    List<Class<?>> newTypeParameters = map.computeIfAbsent(depth + 1, k -> new ArrayList<>());
                    newTypeParameters.add(actualClass);
                }

                if (actualType instanceof ParameterizedType) {
                    exploreTypes(actualType, map, depth + 1);
                }
            }
        }
    }
}

