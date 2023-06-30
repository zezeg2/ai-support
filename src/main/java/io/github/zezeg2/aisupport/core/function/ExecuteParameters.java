package io.github.zezeg2.aisupport.core.function;

import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class ExecuteParameters<T> {
    private String identifier;
    private List<Argument<?>> args;
    private T example;
    private AIModel model;
}
