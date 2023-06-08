package io.github.zezeg2.aisupport.common.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgumentsFactory {

    private final List<Argument<?>> arguments;

    private ArgumentsFactory() {
        arguments = new ArrayList<>();
    }

    public static ArgumentsFactory builder() {
        return new ArgumentsFactory();
    }

    public <T> ArgumentsFactory addArgument(Class<T> type, String fieldName, T value, String desc) {
        arguments.add(new SingleArgument<>(type, fieldName, value, desc, null));
        return this;
    }

    public <T> ArgumentsFactory addArgument(Class<T> type, String fieldName, List<T> value, String desc) {
        arguments.add(new ListArgument<>((Class<List<T>>) (Class<?>) List.class, fieldName, value, desc, type));
        return this;
    }

    public <T> ArgumentsFactory addArgument(Class<T> type, String fieldName, Map<String, T> value, String desc) {
        arguments.add(new MapArgument<>((Class<Map<String, T>>) (Class<?>) Map.class, fieldName, value, desc, type));
        return this;
    }

    public List<Argument<?>> build() {
        return new ArrayList<>(arguments);
    }
}
