package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.argument.MapArgument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportArgumentException;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public abstract class BaseAIFunction<T> implements AIFunction<T> {
    protected final String functionName;
    protected final String description;
    protected final List<Constraint> constraintList;
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
                        return [RESULT] //TODO: RESULT is JsonString of `%s`
                    };
                }
            }
            """;
    protected static final String WHOLE_TEMPLATE = """
            You are now the following Java Lambda function
            ```java
            %s
            // description : This function %s
            %s
            ```
            Constraints
            - Only respond with your `return` value. Do not include any other explanatory text in your response.
            %s
            - Input Format : %s
            - Result Format : %s
            """;

    protected T parseResponse(ChatCompletionResult response) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }

    protected String buildInputFormat(List<Argument<?>> args) throws Exception {
        Map<String, Object> inputDescMap = new LinkedHashMap<>();
        for (Argument<?> argument : args) {
            updateInputDescMap(inputDescMap, argument);
        }

        return convertMapToJson(inputDescMap);
    }


    protected <A> void updateInputDescMap(Map<String, Object> inputDescMap, Argument<A> argument) throws Exception {
        Class<?> argWrapping = argument.getWrapping();

        Map<String, Object> descMap = generateDescMap(argument, argument.getType());
        if (argWrapping == null) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), entry.getValue());
            }
        } else if (argWrapping.equals(WRAPPING.LIST.getValue())) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), List.of(entry.getValue()));
            }

        } else if (argWrapping.equals(WRAPPING.MAP.getValue())) {
            Map<String, Map<String, Object>> transformedMap = new LinkedHashMap<>();
            for (String key : ((MapArgument<A>) argument).getValue().keySet()) {
                transformedMap.put(key, descMap);
            }
            inputDescMap.put(argument.getFieldName(), transformedMap);
        } else {
            throw new NotSupportArgumentException("Argument Wrapping NotSupported");
        }
    }

    protected <A> Map<String, Object> generateDescMap(Argument<A> argument, Class<?> type) throws Exception {
        if (isBaseSupportType(type)) {
            BaseSupportType baseSupportType = (BaseSupportType) type.getConstructor().newInstance();
            return Map.of(argument.getFieldName(), baseSupportType.getExampleMap());
        } else if (argument.getDesc() == null) {
            return Map.of(argument.getFieldName(), argument.getFieldName());
        } else {
            return Map.of(argument.getFieldName(), argument.getDesc());
        }
    }

    protected boolean isBaseSupportType(Class<?> type) {
        return Arrays.stream(type.getInterfaces()).toList().contains(BaseSupportType.class);
    }

    protected String convertMapToJson(Map<String, Object> inputDescMap) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputDescMap);
        } catch (Exception e) {
            throw new CustomJsonException("Failed to convert map to JSON", e);
        }
    }

    protected List<ChatMessage> createMessages(List<Argument<?>> args) throws Exception {
        String executeTemplate = createTemplate(
                resolveRefTypes(args, returnType),
                description,
                createFunctionTemplate(args),
                createConstraints(constraintList),
                buildInputFormat(args),
                buildResultFormat()
        );
        String valuesString = createValuesString(args);
        return List.of(new ChatMessage(ROLE.SYSTEM.getValue(), executeTemplate), new ChatMessage(ROLE.USER.getValue(), valuesString));
    }

    protected String createValuesString(List<Argument<?>> args) throws JsonProcessingException {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.stream().forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueMap);
    }

    protected ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build());
    }

    protected String resolveRefTypes(List<Argument<?>> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }

    protected String createConstraints(List<Constraint> constraintList) {
        return !constraintList.isEmpty() ? constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }
}
