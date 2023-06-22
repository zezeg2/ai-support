package io.github.zezeg2.aisupport.core.reactive.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReactiveAIFunction<T> {
    protected final String functionName;
    protected final String purpose;
    protected final List<Constraint> constraints;
    protected final Class<T> returnType;
    protected final OpenAiService service;
    protected final ObjectMapper mapper;
    protected final ConstructResolver resolver;
    protected final ReactivePromptManager promptManager;
    protected final ReactiveResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    protected AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    protected Mono<Void> init(String identifier, List<Argument<?>> args) {
        try {
            return promptManager.getContext().get(functionName)
                    .hasElement()
                    .flatMap(exists -> {
                        if (!exists) {
                            Prompt prompt = new Prompt(
                                    functionName,
                                    purpose,
                                    resolveRefTypes(args),
                                    createFunction(args),
                                    createConstraints(constraints),
                                    JsonUtils.convertMapToJson(BuildFormatUtil.getArgumentsFormatMap(args)),
                                    buildResultFormat(),
                                    BuildFormatUtil.getFormatString(FeedbackResponse.class)
                            );
                            return promptManager.getContext().savePrompt(functionName, prompt).thenReturn(prompt);
                        }
                        return promptManager.getContext().get(functionName);
                    })
                    .flatMap(prompt -> promptManager.getContext().getPromptChatMessages(functionName, identifier)
                            .map(promptMessages -> promptMessages.getContent().isEmpty()).then(promptManager.addMessage(identifier, functionName, ROLE.SYSTEM, prompt.toString(), ContextType.PROMPT)))
                    .then(promptManager.addMessage(identifier, functionName, ROLE.USER, createValuesString(args), ContextType.PROMPT));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String createValuesString(List<Argument<?>> args) throws JsonProcessingException {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueMap);
    }

    protected String resolveRefTypes(List<Argument<?>> args) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        classList.add(returnType);
        classList.add(FeedbackResponse.class);
        return resolver.resolve(classList);
    }

    protected String createConstraints(List<Constraint> constraintList) {
        return !constraintList.isEmpty() ? constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }


    public String createFunction(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return TemplateConstants.FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, setReturnType());
    }

    protected Mono<T> parseResponseWithValidate(String identifier, ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        return resultValidatorChain.validate(identifier, functionName, content).flatMap((stringResult) -> {
            try {
                return Mono.just(mapper.readValue(stringResult, returnType));
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException(e));
            }
        }).onErrorResume(Mono::error);
    }

    public String buildResultFormat() {
        return BuildFormatUtil.getFormatString(returnType);
    }

    public Mono<T> execute(String identifier, List<Argument<?>> args) {
        AIModel model = getDefaultModel();
        return execute(identifier, args, model);
    }

    public Mono<T> execute(List<Argument<?>> args) {
        AIModel model = getDefaultModel();
        return execute("temp-identifier-" + UUID.randomUUID(), args, model);
    }

    public Mono<T> execute(String identifier, List<Argument<?>> args, AIModel model) {
        return init(identifier, args)
                .then(promptManager.exchangePromptMessages(identifier, functionName, model, true)
                        .flatMap(chatCompletionResult -> parseResponseWithValidate(identifier, chatCompletionResult)));
    }

    protected String setReturnType() {
        return returnType.getSimpleName();
    }
}
