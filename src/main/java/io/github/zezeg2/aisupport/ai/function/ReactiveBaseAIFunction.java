package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactiveSessionContextPromptManager;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.ai.validator.chain.ReactiveResultValidatorChain;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class ReactiveBaseAIFunction<T> implements ReactiveAIFunction<T> {
    protected final String functionName;
    protected final String purpose;
    protected final List<Constraint> constraints;
    protected final Class<T> returnType;
    protected final OpenAiService service;
    protected final ObjectMapper mapper;
    protected final ConstructResolver resolver;
    protected static final String FUNCTION_TEMPLATE = """
            @FunctionalInterface
            public interface FC {
                String %s(%s);
            }
                        
            public class Main {
                public static void main(String[] args) {
                    FC fc = (%s) -> {
                        return [RESULT] //TODO: [RESULT] is JsonString of `%s`
                    };
                }
            }
            """;
    protected final ReactiveSessionContextPromptManager promptManager;
    protected final ReactiveResultValidatorChain resultValidatorChain;
    protected final ExceptionValidator exceptionValidator;

    private final OpenAIProperties openAIProperties;


    @Override
    public Mono<T> execute(ServerWebExchange exchange, List<Argument<?>> args) throws Exception {
        AIModel model = getDefaultModel();
        return execute(exchange, args, model);
    }

    @Override
    public Mono<T> execute(ServerWebExchange exchange, List<Argument<?>> args, AIModel model) {
        return promptManager.getContext().containsPrompt(functionName).hasElement()
                .flatMap(containsPrompt -> {
                    if (!containsPrompt) {
                        String resultFormat = buildResultFormat();
                        if (returnType.equals(STRUCTURE.LIST.getValue())) {
                            resultFormat = String.format("[\n%s,\n]", resultFormat);
                        }
                        if (returnType.equals(STRUCTURE.MAP.getValue())) {
                            resultFormat = String.format("{\n\"key\" : %s,\n}", resultFormat);
                        }
                        ReactivePrompt prompt = new ReactivePrompt(
                                purpose,
                                resolveRefTypes(args),
                                createFunction(args),
                                createConstraints(constraints),
                                JsonUtils.convertMapToJson(BuildFormatUtil.getArgumentsFormatMap(args)),
                                resultFormat,
                                BuildFormatUtil.getFormatString(FeedbackResponse.class),
                                resultValidatorChain.peekValidators(functionName)
                        );
                        promptManager.initPromptContext(exchange, functionName, prompt);
                    } else {
                        promptManager.initPromptContext(exchange, functionName);
                    }

                    return Mono.just(args);
                })
                .flatMap(transferredArgs -> {
                    try {
                        promptManager.addMessage(exchange, functionName, ROLE.USER, createValuesString(transferredArgs));
                        return Mono.just(true);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .flatMap(ignored ->
                        promptManager.exchangeMessages(exchange, functionName, model, true)
                                .flatMap(response -> {
                                    try {
                                        return parseResponseWithValidate(exchange, response);
                                    } catch (Exception e) {
                                        return Mono.error(e);
                                    }
                                }))
                .onErrorResume(e -> Mono.error(new RuntimeException(e)));
    }


    private AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    protected T parseResponse(ChatCompletionResult response) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }

    protected Mono<T> parseResponseWithValidate(ServerWebExchange exchange, ChatCompletionResult response) {
        return Mono.defer(() -> {
            String content = response.getChoices().get(0).getMessage().getContent();
            return resultValidatorChain.validate(exchange, functionName, content).last().<T>handle((stringResult, sink) -> {
                try {
                    sink.next(apply(stringResult));
                } catch (JsonProcessingException e) {
                    sink.error(new RuntimeException(e));
                }
            });
        }).onErrorResume(Mono::error);
    }

    //위에 검토

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

    private T apply(String stringResult) throws JsonProcessingException {

        return mapper.readValue(stringResult, returnType);

    }
}
