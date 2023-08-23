package io.github.zezeg2.aisupport.common.resolver;

import java.util.List;
import java.util.Map;

/**
 * The JavaConstructResolver class implements the ConstructResolver interface
 * and provides a string representation of class maps in Java syntax.
 */
public class JavaConstructResolver implements ConstructResolver {

    /**
     * Converts the given class map to a string representation in Java syntax.
     *
     * @param classMap The class map to convert.
     * @return The string representation of the class map in Java syntax.
     */
    @Override
    public String toString(Map<Class<?>, Map<String, List<String>>> classMap) {
        StringBuilder sb = new StringBuilder();
        for (var classEntry : classMap.entrySet()) {
            if (classEntry.getKey().isEnum()) {
                sb.append("enum ").append(classEntry.getKey().getSimpleName()).append(" {\n");
                for (var fieldEntry : classEntry.getValue().entrySet()) {
                    String fields = String.join(", ", fieldEntry.getValue());
                    sb.append("  ").append(fieldEntry.getKey()).append("(").append(fields).append("),\n");
                }
                sb.setLength(sb.length() - 2);  // remove the last comma
                sb.append(";\n");
            } else {
                sb.append("class ").append(classEntry.getKey().getSimpleName()).append(" {\n");
                for (Map.Entry<String, List<String>> fieldEntry : classEntry.getValue().entrySet()) {
                    sb.append("  ").append(fieldEntry.getValue().get(0)).append(" ").append(fieldEntry.getKey()).append("\n");
                }
            }
            sb.append("}\n");
        }
        return sb.toString();
    }
}
