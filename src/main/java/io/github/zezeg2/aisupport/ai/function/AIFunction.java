package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@Slf4j
public class AIFunction<T> {

    private final String functionName;
    private final String description;
    private final List<Constraint> constraintList;
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final Class<T> returnType;
    private final ConstructResolver resolver;
    private static final String FUNCTION_TEMPLATE = """
            @FunctionalInterface
            public interface FC {
                String %s(%s);
            }
            public class Main {
                public static void main(String[] args) {
                    FC fc = (%s) -> {
                        return [RESULT] //TODO: [RESULT] is JsonString of `%s.class`
                    };
                }
            }
            """;

    public T execute(List<Argument> args, AIModel model) throws Exception {
        List<ChatMessage> messages = createMessages(args);
        String inputFormat = buildInputFormat(args);
        messages.get(0).setContent(messages.get(0).getContent() + "- input format : " + inputFormat);

        if (isBaseSupportType(returnType)) {
            BaseSupportType baseSupportType = (BaseSupportType) returnType.getConstructor().newInstance();
            messages.get(0).setContent(messages.get(0).getContent() +
                    "\n- result format : " +
                    baseSupportType.getExample()
            );
        }

        ChatCompletionResult response = createChatCompletion(model, messages);

        return parseResponse(response);
    }

    private String buildInputFormat(List<Argument> args) throws Exception {
        Map<String, Object> inputDescMap = new LinkedHashMap<>();
        for (Argument argument : args) {
            updateInputDescMap(inputDescMap, argument);
        }
        return convertMapToJson(inputDescMap);
    }

    private void updateInputDescMap(Map<String, Object> inputDescMap, Argument argument) throws Exception {
        Class<?> type = argument.getType();
        Object value = argument.getValue();

        Map<String, Object> descMap = generateDescMap(argument, type);
        if (value instanceof List<?> || value instanceof Set<?>) {
            inputDescMap.put(argument.getFieldName(), List.of(descMap.entrySet().stream().findFirst().get().getValue()));
        } else if ((value instanceof Map<?, ?>)) {
            inputDescMap.put(argument.getFieldName(), ((Map<String, Object>) value).keySet().stream().map(k -> Map.of(k, descMap)));
        } else {
            inputDescMap.put(argument.getFieldName(), descMap);
        }
    }

    private Map<String, Object> generateDescMap(Argument argument, Class<?> type) throws Exception {
        if (isBaseSupportType(type)) {
            return Map.of(argument.getFieldName(), ((BaseSupportType) type.getConstructor().newInstance()).getExampleMap());
        } else if (argument.getDesc() == null) {
            return Map.of(argument.getFieldName(), argument.getFieldName());
        } else {
            return Map.of(argument.getFieldName(), argument.getDesc());
        }
    }

    private boolean isBaseSupportType(Class<?> type) throws Exception {
        return Arrays.stream(type.getInterfaces()).toList().contains(BaseSupportType.class);
    }

    private String convertMapToJson(Map<String, Object> inputDescMap) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputDescMap);
        } catch (Exception e) {
            throw new CustomJsonException("Failed to convert map to JSON", e);
        }
    }

    private List<ChatMessage> createMessages(List<Argument> args) {
        String executeTemplate = createTemplate(
                resolveRefTypes(args, returnType),
                description,
                createFunctionTemplate(returnType, functionName, args),
                createConstraints(constraintList)
        );
        String valuesString = createValuesString(args);
        return List.of(new ChatMessage(ROLE.SYSTEM.getValue(), executeTemplate), new ChatMessage(ROLE.USER.getValue(), valuesString));
    }

    private String createTemplate(String refTypes, String description, String functionTemplate, String constraints) {
        return "You are now the following Java Lambda function: \n"
                + "```java \n"
                + refTypes + "\n"
                + "// description: This function " + description + "\n"
                + functionTemplate + "\n"
                + "```\n"
                + "- Only respond with your `return` value. Do not include any other explanatory text in your response.\n"
                + constraints;
    }

    private String createValuesString(List<Argument> args) {
        return args.stream().map(argument -> {
            String value = argument.getTypeName().equals("String") ? "\"" + argument.getValueToString() + "\"" : argument.getValueToString();
            return argument.getFieldName() + ": " + value;
        }).collect(Collectors.joining("\n"));
    }

    private ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build());
    }

    private T parseResponse(ChatCompletionResult response) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        log.info(content);
        return mapper.readValue(content, returnType);
    }

    private String resolveRefTypes(List<Argument> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }

    private String createConstraints(List<Constraint> constraintList) {
        return constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n"));
    }

    private <T> String createFunctionTemplate(Class<T> returnType, String functionName, List<Argument> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }
}

class CustomJsonException extends RuntimeException {
    public CustomJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}


