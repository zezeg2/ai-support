package io.github.zezeg2.aisupport.common.util;

import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.argument.MapArgument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.Wrapper;
import io.github.zezeg2.aisupport.common.exceptions.CustomInstantiationException;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportedTypeException;
import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import io.github.zezeg2.aisupport.common.type.Supportable;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The BuildFormatUtil class provides utility methods for generating format maps and strings
 * for arguments and return types.
 */
public class BuildFormatUtil {

    /**
     * Generates a format map for a list of arguments.
     * It iterates over the list of arguments and adds their format descriptions to the format map.
     *
     * @param args The list of arguments.
     * @return The format map containing the descriptions of the arguments.
     */
    public static Map<String, Object> getArgumentsFormatMap(List<Argument<?>> args) {
        Map<String, Object> inputDescMap = new LinkedHashMap<>();
        for (Argument<?> argument : args) {
            addArgumentFormat(inputDescMap, argument);
        }
        return inputDescMap;
    }

    public static String getArgumentsFormatMapString(List<Argument<?>> args) {
        return JsonUtil.convertMapToJson(BuildFormatUtil.getArgumentsFormatMap(args));
    }

    /**
     * Creates the constraints String
     * /**
     * Creates the constraints string.
     *
     * @param constraints The list of constraints.
     * @return The constraints string.
     */
    public static String createConstraintsString(List<Constraint> constraints) {
        return !constraints.isEmpty() ? constraints.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }

    /**
     * Adds the format description of an argument to the input description map.
     * It determines the type of wrapping for the argument and generates the corresponding format map.
     * The generated format map is then added to the input description map based on the wrapping type.
     *
     * @param inputDescMap The input description map.
     * @param argument     The argument.
     */
    private static void addArgumentFormat(Map<String, Object> inputDescMap, Argument<?> argument) {
        Class<?> argWrapping = argument.getWrapping();
        Map<String, Object> descMap = generateDescMap(argument);

        if (argWrapping == null) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), entry.getValue());
            }
        } else if (argWrapping.equals(Wrapper.LIST.getValue())) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), List.of(entry.getValue()));
            }
        } else if (argWrapping.equals(Wrapper.MAP.getValue())) {
            Map<String, Map<String, Object>> transformedMap = new LinkedHashMap<>();
            for (String key : ((MapArgument<?>) argument).getValue().keySet()) {
                transformedMap.put(key, descMap);
            }
            inputDescMap.put(argument.getFieldName(), transformedMap);
        } else {
            throw new RuntimeException("This is NotSupported Construct");
        }
    }

    /**
     * Retrieves a temporary instance of the BaseSupportType based on the given type.
     *
     * @param type The type.
     * @return The temporary instance of the BaseSupportType.
     */
    private static BaseSupportType getTempInstance(Class<?> type) {
        BaseSupportType baseSupportType;
        try {
            baseSupportType = (BaseSupportType) type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new CustomInstantiationException(e);
        }
        return baseSupportType;
    }

    /**
     * Generates a format map for an argument based on its type.
     * If the type is a BaseSupportType, the format map of a temporary instance is returned.
     * If the argument does not have a description, the field name is used as the description.
     * Otherwise, the description from the argument is used.
     *
     * @param argument The argument.
     * @return The format map for the argument.
     */
    private static Map<String, Object> generateDescMap(Argument<?> argument) {
        if (isBaseSupportType(argument.getType())) {
            Supportable supportable = getTempInstance(argument.getType());
            return Map.of(argument.getFieldName(), supportable.getFormatMap());
        } else if (argument.getDesc() == null) {
            return Map.of(argument.getFieldName(), argument.getFieldName());
        } else {
            return Map.of(argument.getFieldName(), argument.getDesc());
        }
    }

    /**
     * Checks if a class or its superclasses are of type BaseSupportType.
     *
     * @param clazz The class to check.
     * @return True if the class or its superclasses are of type BaseSupportType, false otherwise.
     */
    public static boolean isBaseSupportType(Class<?> clazz) {
        if (clazz.equals(BaseSupportType.class)) {
            return true;
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null) {
            return false;
        }
        return isBaseSupportType(superClass);
    }

    /**
     * Retrieves the format map for a given return type.
     * If the return type is a BaseSupportType, the format map of a temporary instance is returned.
     * If the return type is not supported, a NotSupportedTypeException is thrown.
     *
     * @param returnType The return type.
     * @return The format map for the return type.
     * @throws NotSupportedTypeException If the return type is not supported.
     */
    public static Map<String, Object> getFormatMap(Class<?> returnType) {
        if (isBaseSupportType(returnType)) {
            return getTempInstance(returnType).getFormatMap();
        } else {
            throw new NotSupportedTypeException();
        }
    }

    /**
     * Retrieves the formatted string representation of a given return type.
     * If the return type is a BaseSupportType, the formatted string of a temporary instance is returned.
     * If the return type is not supported, the simple name of the return type is returned.
     *
     * @param returnType The return type.
     * @return The formatted string representation of the return type.
     */
    public static String getFormatString(Class<?> returnType) {
        if (isBaseSupportType(returnType)) {
            return getTempInstance(returnType).getFormatString();
        } else {
            return returnType.getSimpleName();
        }
    }
}

