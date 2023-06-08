package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactiveFunction<T> extends BaseFunction<Mono<T>, ReactivePromptManager, ReactiveResultValidatorChain> {
    private final Class<T> wrappedType;

    public ReactiveFunction(String functionName, String purpose, List<Constraint> constraints, Class<Mono<T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, ReactivePromptManager promptManager, ReactiveResultValidatorChain resultValidatorChain, ExceptionValidator exceptionValidator, OpenAIProperties openAIProperties, Class<T> wrappedType) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
        this.wrappedType = wrappedType;
    }

    @Override
    protected Mono<T> parseResponseWithValidate(ChatCompletionResult response) {
        return Mono.defer(() -> {
            String content = response.getChoices().get(0).getMessage().getContent();
            return resultValidatorChain.validate(functionName, content).flatMap((stringResult) -> {
                try {
                    return mapper.readValue(stringResult, returnType);
                } catch (JsonProcessingException e) {
                    return Mono.error(new RuntimeException(e));
                }
            });
        }).onErrorResume(Mono::error);
    }

    @Override
    public Mono<T> execute(List<Argument<?>> args) {
        AIModel model = getDefaultModel();
        return execute(args, model);
    }

    @Override
    public Mono<T> execute(List<Argument<?>> args, AIModel model) {
        init(args);
        return promptManager
                .exchangePromptMessages(functionName, model, true)
                .flatMap(this::parseResponseWithValidate);
    }

    @Override
    protected String setReturnType() {
        return wrappedType.getSimpleName();
    }
}
