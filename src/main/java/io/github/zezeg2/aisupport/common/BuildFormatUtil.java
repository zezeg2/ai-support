package io.github.zezeg2.aisupport.common;

import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.argument.MapArgument;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportedConstructException;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportedTypeException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuildFormatUtil {
    public Map<String, Object> getArgumentsFormatMap(List<Argument<?>> args) throws Exception {
        Map<String, Object> inputDescMap = new LinkedHashMap<>();
        for (Argument<?> argument : args) {
            addArgumentFormat(inputDescMap, argument);
        }

        return inputDescMap;
    }

    public void addArgumentFormat(Map<String, Object> inputDescMap, Argument<?> argument) throws Exception {
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

    public Map<String, Object> generateDescMap(Argument<?> argument, Class<?> type) throws Exception {
        if (isBaseSupportType(type)) {
            Supportable supportable = (Supportable) type.getConstructor().newInstance();
            return Map.of(argument.getFieldName(), supportable.getFormatMap());
        } else if (argument.getDesc() == null) {
            return Map.of(argument.getFieldName(), argument.getFieldName());
        } else {
            return Map.of(argument.getFieldName(), argument.getDesc());
        }
    }

    public boolean isBaseSupportType(Class<?> type) {
        return type.getSuperclass().equals(BaseSupportType.class);
    }

    public Map<String, Object> getFormatMap(Class<?> returnType) throws Exception {
        if (isBaseSupportType(returnType))
            return ((BaseSupportType) returnType.getConstructor().newInstance()).getFormatMap();
        else throw new NotSupportedTypeException();
    }

    public String getFormatString(Class<?> returnType) throws Exception {
        if (isBaseSupportType(returnType))
            return ((BaseSupportType) returnType.getConstructor().newInstance()).getFormat();
        else return returnType.getSimpleName();
    }
}
