package io.github.zezeg2.aisupport.common.type;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The SupportableFormatRegistry class is a registry for storing and retrieving format maps of Supportable objects.
 * It provides methods for saving and retrieving format maps associated with Supportable classes.
 * This class allows you to save memory by not creating an object each time you create a format string(Supportable.getFormatMap()).
 *
 * @since 1.0
 */
public class SupportableFormatRegistry {

    /**
     * The registry that holds the format maps of Supportable objects.
     */
    @Getter
    private static final Map<String, Map<String, Object>> registry = new ConcurrentHashMap<>();

    /**
     * Saves the format map for a given Supportable class.
     *
     * @param supportable The Supportable class.
     * @param formatMap   The format map to be saved.
     */
    public static void save(Class<? extends Supportable> supportable, Map<String, Object> formatMap) {
        registry.put(supportable.getSimpleName(), formatMap);
    }

    /**
     * Retrieves the format map for a given Supportable class.
     *
     * @param supportable The Supportable class.
     * @return The format map associated with the Supportable class.
     */
    public static Map<String, Object> getFormat(Class<? extends Supportable> supportable) {
        return registry.get(supportable.getSimpleName());
    }
}
