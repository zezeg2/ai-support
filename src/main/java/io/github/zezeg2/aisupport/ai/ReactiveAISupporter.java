package io.github.zezeg2.aisupport.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.ReactiveAIFunction;
import io.github.zezeg2.aisupport.ai.function.ReactiveAIListFunction;
import io.github.zezeg2.aisupport.ai.function.ReactiveAIMapFunction;
import io.github.zezeg2.aisupport.ai.function.ReactiveAISingleFunction;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactiveSessionContextPromptManager;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.chain.ReactiveResultValidatorChain;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ReactiveAISupporter {
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final ReactiveSessionContextPromptManager promptManager;
    protected final ReactiveResultValidatorChain resultValidatorChain;
    protected final ExceptionValidator exceptionValidator;
    private final OpenAIProperties openAIProperties;


    public <T> ReactiveAIFunction<?> createFunction(STRUCTURE structure, Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return switch (structure) {
            case SINGLE ->
                    new ReactiveAISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
            case LIST ->
                    new ReactiveAIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
            case MAP ->
                    new ReactiveAIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
        };
    }

    public <T> ReactiveAIFunction<T> createSingleFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new ReactiveAISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }

    public <T> ReactiveAIFunction<List<T>> createListFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new ReactiveAIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
    }

    public <T> ReactiveAIFunction<Map<String, T>> createMapFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new ReactiveAIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
    }

}
