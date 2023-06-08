package io.github.zezeg2.aisupport.common;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SupportableFormatRegistry {
    @Getter
    private static final Map<String, Map<String, Object>> registry = new ConcurrentHashMap<>() {
    };

    public static void save(Class<? extends Supportable> supportable, Map<String, Object> formatMap) {
        registry.put(supportable.getSimpleName(), formatMap);
    }

    public static Map<String, Object> getFormat(Class<? extends Supportable> supportable) {
        return registry.get(supportable.getSimpleName());
    }
}
