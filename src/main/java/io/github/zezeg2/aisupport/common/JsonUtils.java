package io.github.zezeg2.aisupport.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;

import java.util.Map;

public class JsonUtils {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static String extractJsonFromMessage(String originalString) {
        int firstIndex = originalString.indexOf('{');
        int lastIndex = originalString.lastIndexOf('}') + 1;

        if (firstIndex != -1) {
            return originalString.substring(firstIndex, lastIndex);
        } else {
            return originalString;
        }
    }

    public static String convertMapToJson(Map<String, Object> inputDescMap) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputDescMap);
        } catch (Exception e) {
            throw new CustomJsonException("Failed to convert map to JSON", e);
        }
    }
}
