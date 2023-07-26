package io.github.zezeg2.aisupport.core.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.context.PromptContextHolder;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessageContext;
import io.github.zezeg2.aisupport.core.validator.ResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The AIFunction class represents a generic AI function that interacts with a chat-based AI system.
 * It is responsible for executing AI tasks, managing prompts, and validating results.
 *
 * @param <T> The type of the return value for the AI function.
 */
@RequiredArgsConstructor
public class AIFunction<T> {
    private final String functionName;
    private final String command;
    private final List<Constraint> constraints;
    private final Class<T> returnType;
    private final ObjectMapper mapper;
    private final PromptManager promptManager;
    private final ResultValidatorChain resultValidatorChain;
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
     * Initializes the AIFunction with the specified execution parameters.
     *
     * @param params The execution parameters.
     */
    private PromptMessageContext init(ExecuteParameters<T> params) {
        List<Argument<?>> args = params.getArgs();
        String identifier = params.getIdentifier();
        T example = params.getExample();
        PromptContextHolder contextHolder = promptManager.getContextHolder();

        Prompt prompt = contextHolder.get(functionName);
        if (prompt == null) {
            prompt = new Prompt(functionName, command, constraints, args, returnType, topP);
            contextHolder.savePrompt(functionName, prompt);
        }

        PromptMessageContext messageContext = contextHolder.createMessageContext(ContextType.PROMPT, functionName, identifier);
        if (messageContext.getMessages().isEmpty()) {
            if (example == null)
                promptManager.addMessageToContext(messageContext, ROLE.SYSTEM, prompt.generate(), ContextType.PROMPT);
            else
                promptManager.addMessageToContext(messageContext, ROLE.SYSTEM, prompt.generate(mapper, example), ContextType.PROMPT);
        }
        promptManager.addMessageToContext(messageContext, ROLE.USER, createArgsString(args), ContextType.PROMPT);
        return messageContext;
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

    private T parseResponseWithValidate(PromptMessageContext messageContext) {
        String content = resultValidatorChain.validate(messageContext);
        try {
            return mapper.readValue(content, returnType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the AIFunction with the specified execution parameters.
     *
     * @param params The execution parameters.
     * @return The result of the execution.
     */
    public T execute(ExecuteParameters<T> params) {
        if (params.getModel() == null) params.setModel(getDefaultModel());
        if (params.getIdentifier() == null) params.setIdentifier("temp-identifier-" + UUID.randomUUID());
        PromptMessageContext promptMessageContext = init(params);
        PromptMessageContext response = promptManager.exchangeMessages(ContextType.PROMPT, promptMessageContext, params.getModel(), topP, true);
        return parseResponseWithValidate(response);
    }
}

