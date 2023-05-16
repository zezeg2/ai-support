package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.BaseSupportType;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
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

    public T execute(List<Argument> args, AIModel model) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<ChatMessage> messages = createMessages(args);
        if (Arrays.stream(returnType.getInterfaces()).toList().contains(BaseSupportType.class)){
            BaseSupportType baseSupportType = (BaseSupportType) returnType.getConstructor().newInstance();
            messages.get(0).setContent(messages.get(0).getContent() +
                    "- result example : " +
                    baseSupportType.getExample());
        }
        ChatCompletionResult response = createChatCompletion(model, messages);

        return parseResponse(response);
    }

    private List<ChatMessage> createMessages(List<Argument> args) {
        String executeTemplate = createTemplate(description,
                resolveRefTypes(args, returnType), createFunctionTemplate(returnType, functionName, args),
                createConstraints(constraintList));
        String valuesString = createValuesString(args);
        return List.of(new ChatMessage(ROLE.SYSTEM.getValue(), executeTemplate), new ChatMessage(ROLE.USER.getValue(), valuesString));
    }
    private static String createTemplate(String description, String refTypes, String functionTemplate, String constraints) {
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
            String value = argument.getType().equals("String") ? "\"" + argument.getValue() + "\"" : argument.getValue();
            return argument.getField() + ": " + value;
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
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }

    private String resolveRefTypes(List<Argument> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::type).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }


    private String createConstraints(List<Constraint> constraintList) {
        return constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n"));
    }

    private <T> String createFunctionTemplate(Class<T> returnType, String functionName, List<Argument> args) {
        String fieldsString = args.stream().map(Argument::getField).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getType() + " " + argument.getField())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }
}
