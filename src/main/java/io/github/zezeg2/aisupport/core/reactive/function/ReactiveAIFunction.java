package io.github.zezeg2.aisupport.core.reactive.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.Role;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.common.type.SimpleResult;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import io.github.zezeg2.aisupport.core.function.ExecuteParameters;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessageContext;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The ReactiveAIFunction class represents a reactive version of an AI function that interacts with a chat-based AI system.
 * It is responsible for executing AI tasks, managing prompts, and validating results using reactive programming principles.
 *
 * @param <T> The type of the return value for the AI function.
 */
@RequiredArgsConstructor
public class ReactiveAIFunction<T> {
    private final String functionName;
    private final String role;
    private final String command;
    private final List<Constraint> constraints;
    private final Class<T> returnType;
    private final double topP;
    private final ObjectMapper mapper;
    private final ReactivePromptManager promptManager;
    private final ReactiveResultValidatorChain resultValidatorChain;
    private final ConstructResolver resolver;
    private final OpenAIProperties openAIProperties;

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
    private Mono<PromptMessageContext> init(ExecuteParameters<T> params) {
        List<Argument<?>> args = params.getArgs();
        String identifier = params.getIdentifier();
        T example = params.getExample();
        ReactivePromptContextHolder contextHolder = promptManager.getContextHolder();
        return contextHolder.get(functionName)
                .switchIfEmpty(Mono.just(new Prompt(functionName, this.role == null ? "" : this.role, command, constraints, args, returnType, topP, resolver))
                        .flatMap(prompt -> contextHolder.savePrompt(functionName, prompt).thenReturn(prompt)))
                .flatMap(prompt -> contextHolder.<PromptMessageContext>createMessageContext(ContextType.PROMPT, functionName, identifier)
                        .flatMap(promptMessageContext -> {
                            promptMessageContext.setUserInput(createArgsMap(args));
                            promptMessageContext.setModel(params.getModel());
                            try {
                                promptManager.addMessageToContext(ContextType.PROMPT, promptMessageContext, Role.SYSTEM, prompt.generate(example == null ? "" : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(example)));
                            } catch (JsonProcessingException e) {
                                return Mono.error(new CustomJsonException(e));
                            }
                            promptManager.addMessageToContext(ContextType.PROMPT, promptMessageContext, Role.USER, createArgsString(args));
                            return Mono.just(promptMessageContext);
                        })
                );
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
     * @param promptMessageContext Prompt message context for calling openai chat completion api.
     * @return A Mono that emits the parsed and validated response object.
     */

    private Mono<T> parseResponseWithValidate(PromptMessageContext promptMessageContext) {
        return resultValidatorChain.validate(promptMessageContext)
                .flatMap((validatedResult) -> {
                    try {
                        return Mono.just(mapper.readValue(validatedResult, returnType));
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
                .flatMap(promptMessageContext -> promptManager.exchangeMessages(ContextType.PROMPT, promptMessageContext, params.getModel(), topP, true)
                        .flatMap(chatCompletionResult -> parseResponseWithValidate(promptMessageContext)));
    }

    public Mono<SimpleResult<T>> executeAndCountBill(ExecuteParameters<T> params) {
        if (params.getModel() == null) params.setModel(getDefaultModel());
        if (params.getIdentifier() == null) params.setIdentifier("temp-identifier-" + UUID.randomUUID());
        return init(params)
                .flatMap(promptMessageContext -> promptManager.exchangeMessages(ContextType.PROMPT, promptMessageContext, params.getModel(), topP, true).ofType(PromptMessageContext.class)
                        .flatMap(response -> parseResponseWithValidate(response)
                                .flatMap(result -> promptManager.getExecutionBill(response)
                                        .flatMap(bill -> Mono.just(SimpleResult.<T>builder().result(result).bill(bill).build()))
                                )
                        )
                );
    }
}

