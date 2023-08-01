package io.github.zezeg2.aisupport.common.resolver;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * The ConstructResolver interface provides methods for resolving and generating class maps.
 * It is used to convert class information into a string representation.
 */
public interface ConstructResolver {

    /**
     * Converts the given class map to a string representation.
     *
     * @param classMap The class map to convert.
     * @return The string representation of the class map.
     */
    String toString(Map<String, Map<String, List<String>>> classMap);

    /**
     * Resolves the given class and returns its string representation.
     *
     * @param clazz The class to resolve.
     * @return The string representation of the resolved class.
     */
    default String resolve(Class<?> clazz) {
        return toString(generateClassMap(Collections.singleton(clazz), null));
    }

    /**
     * Resolves the given set of classes and returns their string representation.
     *
     * @param classSet The set of classes to resolve.
     * @return The string representation of the resolved classes.
     */
    default String resolve(Set<Class<?>> classSet) {
        return toString(generateClassMap(classSet, null));
    }

    /**
     * Generates a class map for the given set of classes.
     *
     * @param classSet The set of classes to generate the class map for.
     * @param classMap The existing class map to populate (optional).
     * @return The generated class map.
     */
    default Map<String, Map<String, List<String>>> generateClassMap(Set<Class<?>> classSet, Map<String, Map<String, List<String>>> classMap) {
        if (classMap == null) {
            classMap = new HashMap<>();
        }

        for (Class<?> clazz : classSet) {
            if (clazz.getSuperclass() == null) {
                continue;
            }
            if (clazz.equals(Object.class) ||
                    clazz.equals(Mono.class) ||
                    clazz.equals(Flux.class) ||
                    clazz.equals(String.class) ||
                    clazz.getSuperclass().equals(Number.class) ||
                    clazz.getSuperclass().equals(Collection.class) ||
                    clazz.getSuperclass().equals(Map.class) ||
                    clazz.getSuperclass().equals(HashMap.class) ||
                    clazz.getSuperclass().equals(Set.class) ||
                    clazz.getSuperclass().equals(HashSet.class) ||
                    clazz.getSuperclass().equals(List.class)) {
                continue;
            }

            Map<String, List<String>> fieldsMap = classMap.getOrDefault(clazz.getSimpleName(), new HashMap<>());
            Class<?> currentClass = clazz;
            while (currentClass != null && !currentClass.equals(Object.class)) {
                for (Field field : currentClass.getDeclaredFields()) {
                    addFieldToMap(classMap, fieldsMap, field);
                }
                currentClass = currentClass.getSuperclass();
            }
            classMap.put(clazz.getSimpleName(), fieldsMap);
        }

        return classMap;
    }

    /**
     * Adds a field to the class map.
     *
     * @param classMap  The class map to populate.
     * @param fieldsMap The map of fields.
     * @param field     The field to add.
     */
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

    /**
     * Adds the parameterized type information to the field information.
     *
     * @param fieldInfo         The list of field information.
     * @param temp              The list of temporary classes.
     * @param parameterizedType The parameterized type.
     * @param type              The field type.
     */
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

    /**
     * Analyzes non-primitive classes recursively and generates their class maps.
     *
     * @param classMap The class map to populate.
     * @param classes  The list of classes to analyze.
     */
    default void analyzeNonPrimitiveClasses(Map<String, Map<String, List<String>>> classMap, List<Class<?>> classes) {
        classes.stream()
                .filter(t -> !t.isPrimitive() && !String.class.equals(t) && !Number.class.isAssignableFrom(t) && !Object.class.equals(t))
                .forEach(t -> generateClassMap(Collections.singleton(t), classMap));
    }
}




