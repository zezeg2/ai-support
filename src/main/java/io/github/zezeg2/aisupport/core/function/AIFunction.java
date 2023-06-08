package io.github.zezeg2.aisupport.core.function;

import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;

import java.util.List;

public interface AIFunction<T> {

    T execute(List<Argument<?>> args) throws Exception;

    T execute(List<Argument<?>> args, AIModel model) throws Exception;

    String buildResultFormat();

    String createFunction(List<Argument<?>> args);
}
