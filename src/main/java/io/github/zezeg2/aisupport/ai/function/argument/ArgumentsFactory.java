package io.github.zezeg2.aisupport.ai.function.argument;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;

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

    public <T> ArgumentsFactory addArgument(WRAPPING wrapping, Class<T> type, String fieldName, Object value) {
        arguments.add(wrapping.equals(WRAPPING.NONE) ? new SingleArgument<>(wrapping, type, fieldName, value)
                : wrapping.equals(WRAPPING.LIST) ? new ListArgument<>(wrapping, type, fieldName, (List<T>) value)
                : wrapping.equals(WRAPPING.MAP) ? new MapArgument<>(wrapping, type, fieldName, (Map<String, T>) value)
                : null
        );
        return this;
    }

    public <T> ArgumentsFactory addArgument(WRAPPING wrapping, Class<T> type, String fieldName, Object value, String desc) {
        arguments.add(wrapping.equals(WRAPPING.NONE) ? new SingleArgument<>(wrapping, type, fieldName, value, desc)
                : wrapping.equals(WRAPPING.LIST) ? new ListArgument<>(wrapping, type, fieldName, (List<T>) value, desc)
                : wrapping.equals(WRAPPING.MAP) ? new MapArgument<>(wrapping, type, fieldName, (Map<String, T>) value, desc)
                : null
        );
        return this;
    }

    public List<Argument<?>> build() {
        return new ArrayList<>(arguments);
    }
}