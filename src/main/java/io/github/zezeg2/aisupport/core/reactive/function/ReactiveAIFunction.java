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
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import io.github.zezeg2.aisupport.core.function.ExecuteParameters;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * The ReactiveAIFunction class represents a reactive version of an AI function that interacts with a chat-based AI system.
 * It is responsible for executing AI tasks, managing prompts, and validating results using reactive programming principles.
 *
 * @param <T> The type of the return value for the AI function.
 */
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

    /**
     * Retrieves the default AI model.
     *
     * @return The default AI model.
     */
    private AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    /**
     * Initializes the ReactiveAIFunction with the specified execution parameters using reactive operations.
     *
     * @param params The execution parameters.
     * @return A Mono that represents the completion of the initialization process.
     */
    private Mono<Void> init(ExecuteParameters<T> params) {
        List<Argument<?>> args = params.getArgs();
        String identifier = params.getIdentifier();
        T example = params.getExample();
        ReactivePromptContextHolder contextHolder = promptManager.getContextHolder();

        return contextHolder.get(functionName)
                .switchIfEmpty(Mono.just(new Prompt(functionName, command, constraints, args, returnType, topP))
                        .flatMap(prompt -> contextHolder.savePrompt(functionName, prompt).thenReturn(prompt)))
                .flatMap(prompt -> contextHolder.getContext(ContextType.PROMPT, functionName, identifier)
                        .flatMap(messageContext -> {
                            if (messageContext.getMessages().isEmpty()) {
                                return example == null ?
                                        promptManager.addMessageToContext(functionName, identifier, ROLE.SYSTEM, prompt.generate(), ContextType.PROMPT) :
                                        promptManager.addMessageToContext(functionName, identifier, ROLE.SYSTEM, prompt.generate(mapper, example), ContextType.PROMPT);
                            }
                            if (example == null) return Mono.empty();
                            else {
                                Optional<ChatMessage> systemMessage = messageContext.getMessages().stream()
                                        .filter(message -> message.getRole().equals(ROLE.SYSTEM.getValue()))
                                        .findFirst();
                                systemMessage.ifPresent(message -> message.setContent(prompt.generate(mapper, example)));
                                return contextHolder.saveContext(ContextType.PROMPT, messageContext);
                            }
                        })
                )
                .then(promptManager.addMessageToContext(functionName, identifier, ROLE.USER, createArgsString(args), ContextType.PROMPT));
    }

    /**
     * Creates a string representation of the arguments for the AI function.
     *
     * @param args The list of arguments for the AI function.
     * @return The string representation of the arguments.
     */
    private String createArgsString(List<Argument<?>> args) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(createArgsMap(args));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a map of argument names and values for the AI function.
     *
     * @param args The list of arguments for the AI function.
     * @return The map of argument names and values.
     */
    private Map<String, Object> createArgsMap(List<Argument<?>> args) {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));
        return valueMap;
    }

    /**
     * Parses the response from the AI model and validates it using the result validator chain in a reactive manner.
     *
     * @param params   The execution parameters.
     * @param response The chat completion result.
     * @return A Mono that emits the parsed and validated response object.
     */
    private Mono<T> parseResponseWithValidate(ExecuteParameters<T> params, ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        return resultValidatorChain.validate(functionName, params.getIdentifier(), content)
                .flatMap((stringResult) -> {
                    try {
                        return Mono.just(mapper.readValue(stringResult, returnType));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                })
                .onErrorResume(Mono::error);
    }

    /**
     * Executes the ReactiveAIFunction with the specified execution parameters using reactive operations.
     *
     * @param params The execution parameters.
     * @return A Mono that emits the result of the execution.
     */
    public Mono<T> execute(ExecuteParameters<T> params) {
        if (params.getModel() == null) params.setModel(getDefaultModel());
        if (params.getIdentifier() == null) params.setIdentifier("temp-identifier-" + UUID.randomUUID());
        return init(params)
                .then(promptManager.exchangeMessages(ContextType.PROMPT, functionName, params.getIdentifier(), params.getModel(), topP, true)
                        .flatMap(chatCompletionResult -> parseResponseWithValidate(params, chatCompletionResult)));
    }
}

