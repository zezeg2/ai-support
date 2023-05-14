package io.github.zezeg2.aisupport.resolver;

import java.util.List;
import java.util.Map;

public class JAVAConstructResolver implements ConstructResolver {

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
