package io.github.zezeg2.aisupport.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ReflectionUtils class provides utility methods for working with reflection in Java.
 * It includes methods for retrieving parameter types and their simplified names.
 *
 * @since 1.0
 */
public class ReflectionUtils {

    /**
     * Retrieves a map of parameter types for a given class.
     * The map contains the parameter types organized by their depth in the class hierarchy.
     *
     * @param clazz The class to retrieve the parameter types for.
     * @return A map of parameter types organized by their depth.
     */
    public static Map<Integer, List<Class<?>>> getParameterTypesMap(Class<?> clazz) {
        Type type = clazz.getGenericSuperclass();
        Map<Integer, List<Class<?>>> map = new HashMap<>();
        exploreTypes(type, map, 1);
        return map;
    }

    /**
     * Retrieves a map of simplified names of parameter types for a given class.
     * The map contains the simplified names of parameter types organized by their depth in the class hierarchy.
     *
     * @param clazz The class to retrieve the parameter types for.
     * @return A map of simplified names of parameter types organized by their depth.
     */
    public static Map<Integer, List<String>> getParameterTypesSimpleNameMap(Class<?> clazz) {
        return getParameterTypesMap(clazz).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(Class::getSimpleName).toList(),
                        (a, b) -> b));
    }

    /**
     * Recursively explores the types in a given type, populating the map with parameter types and their depths.
     *
     * @param type  The type to explore.
     * @param map   The map to populate with parameter types.
     * @param depth The current depth in the class hierarchy.
     */
    private static void exploreTypes(Type type, Map<Integer, List<Class<?>>> map, int depth) {
        if (type instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            List<Class<?>> typeParameters = map.computeIfAbsent(depth, k -> new ArrayList<>());
            typeParameters.add(rawType);

            for (Type actualType : parameterizedType.getActualTypeArguments()) {
                if (actualType instanceof Class<?> actualClass) {
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

