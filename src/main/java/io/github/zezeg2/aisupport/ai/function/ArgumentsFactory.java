package io.github.zezeg2.aisupport.ai.function;

import java.util.ArrayList;
import java.util.List;

public class ArgumentsFactory {

    private final List<Argument> arguments;

    private ArgumentsFactory() {
        arguments = new ArrayList<>();
    }

    public static ArgumentsFactory builder() {
        return new ArgumentsFactory();
    }

    public ArgumentsFactory addArgument(String field, Object value, Class<?> type) {
        arguments.add(new Argument(field, value, type));
        return this;
    }

    public ArgumentsFactory addArgument(String field, Object value, Class<?> type, String desc) {
        arguments.add(new Argument(field, value, type, desc));
        return this;
    }

    public List<Argument> build() {
        return new ArrayList<>(arguments);
    }
}
