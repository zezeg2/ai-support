package io.github.zezeg2.aisupport.core.reactive.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.BaseAIFunction;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.ReactiveResultValidatorChain;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactiveAIFunction<T> extends BaseAIFunction<Mono<T>, ReactivePromptManager, ReactiveResultValidatorChain> {
    private final Class<T> wrappedType;

    public ReactiveAIFunction(String functionName, String purpose, List<Constraint> constraints, Class<Mono<T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, ReactivePromptManager promptManager, ReactiveResultValidatorChain resultValidatorChain, OpenAIProperties openAIProperties, Class<T> wrappedType) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, promptManager, resultValidatorChain, openAIProperties);
        this.wrappedType = wrappedType;
    }

    @Override
    protected Mono<T> parseResponseWithValidate(ChatCompletionResult response) {
        return Mono.defer(() -> {
            String content = response.getChoices().get(0).getMessage().getContent();
            return resultValidatorChain.validate(functionName, content).flatMap((stringResult) -> {
                try {
                    return Mono.just(mapper.readValue(stringResult, wrappedType));
                } catch (JsonProcessingException e) {
                    return Mono.error(new RuntimeException(e));
                }
            });
        }).onErrorResume(Mono::error);
    }

    @Override
    public String buildResultFormat() {
        return BuildFormatUtil.getFormatString(wrappedType);
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
