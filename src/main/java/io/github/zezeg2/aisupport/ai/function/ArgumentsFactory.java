package io.github.zezeg2.aisupport.ai.function;

import io.github.zezeg2.aisupport.common.enums.WRAPPING;

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

    public ArgumentsFactory addArgument(WRAPPING wrapping,  Class<?> type, String field, Object value) {
        arguments.add(new Argument(wrapping, type, field, value));
        return this;
    }

    public ArgumentsFactory addArgument(WRAPPING wrapping,  Class<?> type, String field, Object value, String desc) {
        arguments.add(new Argument(wrapping, type, field, value, desc));
        return this;
    }

    public List<Argument> build() {
        return new ArrayList<>(arguments);
    }
}
