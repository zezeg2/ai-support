package com.jbyee.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSConstructResolver implements ConstructResolver {
    @Override
    public String resolve(Class<?> clazz) {
        Map<String, Map<String, List<String>>> classMap = new HashMap<>();
        genClassMap(clazz, classMap);
        return toString(classMap);
    }

    private String toString(Map<String, Map<String, List<String>>> classMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, List<String>>> classEntry : classMap.entrySet()) {
            sb.append("type ").append(classEntry.getKey()).append(" = {\n");
            for (Map.Entry<String, List<String>> fieldEntry : classEntry.getValue().entrySet()) {
                sb.append("  ").append(fieldEntry.getKey()).append(": ").append(convert(fieldEntry.getValue().get(0))).append("\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    private String convert(String typeName) {
        List<String> numbers = List.of("byte", "short", "int", "long", "float", "double",
                "Byte", "Short", "Integer", "Long", "Float", "Double",
                "BigInteger", "BigDecimal");
        if (numbers.contains(typeName)) return "number";
        if (typeName.equals("String")) return "string";
        if (typeName.equals("boolean")) return "boolean";
        if (typeName.equals("char")) return "string"; // TypeScript doesn't have a char, so we use string
        if (typeName.endsWith("[]")) return convert(typeName.substring(0, typeName.length() - 2)) + "[]"; // array types
        // you may need to add more conditions here to handle other types
        return typeName + " | undefined"; // for complex types, we keep the original name
    }

}
