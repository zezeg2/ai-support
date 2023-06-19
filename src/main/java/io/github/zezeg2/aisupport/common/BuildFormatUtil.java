package io.github.zezeg2.aisupport.common;

import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.argument.MapArgument;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportedConstructException;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportedTypeException;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuildFormatUtil {
    public static Map<String, Object> getArgumentsFormatMap(List<Argument<?>> args) {
        Map<String, Object> inputDescMap = new LinkedHashMap<>();
        for (Argument<?> argument : args) {
            addArgumentFormat(inputDescMap, argument);
        }

        return inputDescMap;
    }

    private static void addArgumentFormat(Map<String, Object> inputDescMap, Argument<?> argument) {
        Class<?> argWrapping = argument.getWrapping();

        Map<String, Object> descMap = generateDescMap(argument, argument.getType());
        if (argWrapping == null) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), entry.getValue());
            }
        } else if (argWrapping.equals(STRUCTURE.LIST.getValue())) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), List.of(entry.getValue()));
            }

        } else if (argWrapping.equals(STRUCTURE.MAP.getValue())) {
            Map<String, Map<String, Object>> transformedMap = new LinkedHashMap<>();
            for (String key : ((MapArgument<?>) argument).getValue().keySet()) {
                transformedMap.put(key, descMap);
            }
            inputDescMap.put(argument.getFieldName(), transformedMap);
        } else {
            throw new NotSupportedConstructException();
        }
    }

    private static BaseSupportType getTempInstance(Class<?> type) {
        BaseSupportType supportable;
        try {
            supportable = (BaseSupportType) type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            String errorMessage = "Error occurred while generating description map: " + e.getMessage();
            throw new RuntimeException(errorMessage, e);
        }
        return supportable;
    }

    private static Map<String, Object> generateDescMap(Argument<?> argument, Class<?> type) {
        if (isBaseSupportType(type)) {
            Supportable supportable = getTempInstance(type);
            return Map.of(argument.getFieldName(), supportable.getFormatMap());
        } else if (argument.getDesc() == null) {
            return Map.of(argument.getFieldName(), argument.getFieldName());
        } else {
            return Map.of(argument.getFieldName(), argument.getDesc());
        }
    }

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

    public static Map<String, Object> getFormatMap(Class<?> returnType) {
        if (isBaseSupportType(returnType))
            return getTempInstance(returnType).getFormatMap();
        else throw new NotSupportedTypeException();
    }

    public static String getFormatString(Class<?> returnType) {
        if (isBaseSupportType(returnType))
            return getTempInstance(returnType).getFormat();
        else return returnType.getSimpleName();
    }
}
