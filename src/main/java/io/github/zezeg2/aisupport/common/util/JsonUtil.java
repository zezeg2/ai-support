package io.github.zezeg2.aisupport.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;

import java.util.Map;

/**
 * Utility class for JSON-related operations.
 *
 * @since 1.0
 */
public class JsonUtil {

    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * Extracts the JSON portion from a given string.
     * It searches for the first occurrence of '{' and the last occurrence of '}' in the original string,
     * and returns the substring between them (inclusive of the curly braces).
     * If no curly braces are found, it returns the original string as is.
     *
     * @param originalString The original string containing JSON.
     * @return The extracted JSON portion from the original string.
     */
    public static String extractJsonFromMessage(String originalString) {
        int firstIndex = originalString.indexOf('{');
        int lastIndex = originalString.lastIndexOf('}') + 1;

        if (firstIndex != -1) {
            return originalString.substring(firstIndex, lastIndex);
        } else {
            return originalString;
        }
    }

    /**
     * Converts a map to its JSON representation.
     * It uses the ObjectMapper to serialize the input map to a JSON string.
     * The resulting JSON string is formatted with default pretty printing.
     *
     * @param inputDescMap The map to be converted to JSON.
     * @return The JSON representation of the input map.
     * @throws CustomJsonException If an exception occurs during the conversion process.
     */
    public static String convertMapToJson(Map<String, Object> inputDescMap) throws CustomJsonException {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputDescMap);
        } catch (Exception e) {
            throw new CustomJsonException("Failed to convert map to JSON", e);
        }
    }
}
