package io.github.zezeg2.aisupport.core.reactive.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.ExecuteParameters;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ReactiveAIFunction<T> {
    private final String functionName;
    private final String command;

    private final List<Constraint> constraints;

    private final Class<T> returnType;
    private final ObjectMapper mapper;
    private final ReactivePromptManager promptManager;
    private final ReactiveResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;
    private final double topP;

    private AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    private Mono<Void> init(ExecuteParameters<T> params) {
        List<Argument<?>> args = params.getArgs();
        String identifier = params.getIdentifier();
        T example = params.getExample();
        return promptManager.getContext().get(functionName)
                .switchIfEmpty(Mono.just(new Prompt(functionName, command, constraints, args, returnType, topP))
                        .flatMap(prompt -> promptManager.getContext().savePrompt(functionName, prompt).thenReturn(prompt)))
                .flatMap(prompt -> promptManager.getContext().getPromptChatMessages(functionName, identifier)
                        .map(promptMessages -> promptMessages.getContent().isEmpty())
                        .flatMap(isEmpty -> isEmpty ? example == null ?
                                promptManager.addMessage(functionName, identifier, ROLE.SYSTEM, prompt.generate(), ContextType.PROMPT) :
                                promptManager.addMessage(functionName, identifier, ROLE.SYSTEM, prompt.generate(mapper, example), ContextType.PROMPT) :
                                Mono.empty())
                        .thenReturn(prompt)
                )
                .flatMap(prompt -> promptManager.getContext().getPromptChatMessages(functionName, identifier)
                        .flatMap(promptMessages -> {
                            if (example == null) return Mono.empty();
                            ChatMessage systemMessage = promptMessages.getContent().stream().filter(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue())).findFirst().orElseThrow();
                            String generatedSystemMessageContent = prompt.generate(mapper, example);
                            if (systemMessage.getContent().equals(generatedSystemMessageContent))
                                return Mono.empty();
                            systemMessage.setContent(generatedSystemMessageContent);
                            return promptManager.getContext().savePromptMessages(functionName, identifier, systemMessage);
                        })
                )
                .then(promptManager.addMessage(functionName, identifier, ROLE.USER, createArgsString(args), ContextType.PROMPT));
    }


    private String createArgsString(List<Argument<?>> args) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(createArgsMap(args));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> createArgsMap(List<Argument<?>> args) {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));
        return valueMap;
    }

    private Mono<T> parseResponseWithValidate(ExecuteParameters<T> params, ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        return resultValidatorChain.validate(functionName, params.getIdentifier(), content).flatMap((stringResult) -> {
            try {
                return Mono.just(mapper.readValue(stringResult, returnType));
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException(e));
            }
        }).onErrorResume(Mono::error);
    }

    public Mono<T> execute(ExecuteParameters<T> params) {
        if (params.getModel() == null) params.setModel(getDefaultModel());
        if (params.getIdentifier() == null) params.setIdentifier("temp-identifier-" + UUID.randomUUID());
        return init(params)
                .then(promptManager.exchangePromptMessages(functionName, params.getIdentifier(), params.getModel(), topP, true)
                        .flatMap(chatCompletionResult -> parseResponseWithValidate(params, chatCompletionResult)));
    }
}
