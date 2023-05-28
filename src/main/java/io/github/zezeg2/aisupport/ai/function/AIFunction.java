package io.github.zezeg2.aisupport.ai.function;

import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.model.AIModel;

import java.util.List;

public interface AIFunction<T> {

    T execute(List<Argument<?>> args, AIModel model) throws Exception;

    String buildResultFormat() throws Exception;

    String createFunction(List<Argument<?>> args);
}
