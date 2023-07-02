package io.github.zezeg2.aisupport.common.resolver;

import java.util.List;
import java.util.Map;

/**
 * The JavaConstructResolver class implements the ConstructResolver interface
 * and provides a string representation of class maps in Java syntax.
 *
 * @since 1.0
 */
public class JavaConstructResolver implements ConstructResolver {

    /**
     * Converts the given class map to a string representation in Java syntax.
     *
     * @param classMap The class map to convert.
     * @return The string representation of the class map in Java syntax.
     */
    @Override
    public String toString(Map<String, Map<String, List<String>>> classMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, List<String>>> classEntry : classMap.entrySet()) {
            sb.append("class ").append(classEntry.getKey()).append(" {\n");
            for (Map.Entry<String, List<String>> fieldEntry : classEntry.getValue().entrySet()) {
                sb.append("  ").append(fieldEntry.getValue().get(0)).append(" ").append(fieldEntry.getKey()).append("\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }
}
