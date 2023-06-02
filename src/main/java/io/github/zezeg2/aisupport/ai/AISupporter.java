package io.github.zezeg2.aisupport.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.AIFunction;
import io.github.zezeg2.aisupport.ai.function.AIListFunction;
import io.github.zezeg2.aisupport.ai.function.AIMapFunction;
import io.github.zezeg2.aisupport.ai.function.AISingleFunction;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AISupporter {
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final PromptManager promptManager;
    protected final ResultValidatorChain resultValidatorChain;
    protected final ExceptionValidator exceptionValidator;
    private final OpenAIProperties openAIProperties;


    public <T> AIFunction<?> createFunction(STRUCTURE structure, Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return switch (structure) {
            case SINGLE ->
                    new AISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
            case LIST ->
                    new AIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
            case MAP ->
                    new AIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
        };
    }

    public <T> AIFunction<T> createSingleFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new AISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }

    public <T> AIFunction<List<T>> createListFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new AIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
    }

    public <T> AIFunction<Map<String, T>> createMapFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new AIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties, returnType);
    }
}
