package io.github.zezeg2.aisupport.common;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


public class FormatStore {
    @Getter
    private static final Map<String, Map<String, Object>> registry = new HashMap<>();

    public static void save(Class<? extends Supportable> supportable, Map<String, Object> formatMap) {
        registry.put(supportable.getSimpleName(), formatMap);
    }

    public static Map<String, Object> getFormat(Class<? extends Supportable> supportable) {
        return registry.get(supportable.getSimpleName());
    }
}
