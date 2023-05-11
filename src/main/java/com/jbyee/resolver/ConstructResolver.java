package com.jbyee.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public interface ConstructResolver {
    String resolve(Class<?> clazz);

    default void genClassMap(Class<?> clazz, Map<String, Map<String, List<String>>> classMap) {
        Map<String, List<String>> fieldsMap = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            Type genericType = field.getGenericType();
            List<String> fieldInfo = new ArrayList<>();
            List<Class<?>> temp = new ArrayList<>();

            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                //Not Allowed Overlapping Generic yet
                String[] collect = Arrays.stream(typeArguments).map(typeArgument -> {
                    temp.add(((Class) typeArgument).getNestHost());
                    String[] split = typeArgument.getTypeName().split("\\.");
                    return split[split.length - 1];
                }).toList().toArray(new String[0]);

                fieldInfo.add(String.format("%s<%s>", type.getSimpleName(), String.join(", ", collect)));


            } else {
                fieldInfo.add(type.getSimpleName());
                type = field.getType().getComponentType() == null ? type : type.getComponentType();
                temp.add(type);
            }

            temp.forEach(t -> {
                if (!t.isPrimitive() && !String.class.equals(t) && !Number.class.isAssignableFrom(t) && !Object.class.equals(t)) {
                    genClassMap(t, classMap);
                }
            });

            fieldsMap.put(field.getName(), fieldInfo);
        }

        classMap.put(clazz.getSimpleName(), fieldsMap);
    }

}
