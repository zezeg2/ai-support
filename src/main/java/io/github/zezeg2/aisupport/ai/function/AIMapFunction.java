package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.validate.Validator;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptContext;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AIMapFunction<T> extends BaseAIFunction<Map<String, T>> {
    public AIMapFunction(String functionName, String description, List<Constraint> constraintList, Class<Map<String, T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, Class<T> wrappedType) {
        super(functionName, description, constraintList, returnType, service, mapper, resolver);
        this.wrappedType = wrappedType;
    }

    public AIMapFunction(String functionName, String purpose, List<Constraint> constraintList, Class<Map<String, T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, List<Validator> validators, Class<T> wrappedType) {
        super(functionName, purpose, constraintList, returnType, service, mapper, resolver, validators);
        this.wrappedType = wrappedType;
    }

    private final Class<T> wrappedType;

    @Override
    public Map<String, T> execute(List<Argument<?>> args, AIModel model) throws Exception {
        List<ChatMessage> messages = createMessages(args);
        ChatCompletionResult response = createChatCompletion(model, messages);
        return parseResponse(response);
    }

    @Override
    public Map<String, T> executeWithContext(List<Argument<?>> args, AIModel model) throws Exception {
        initIfEmptyContext(args);
        addMessage(ROLE.USER, createValuesString(args));
        List<ChatMessage> contextMessages = PromptContext.getPromptMessageContext(functionName).get(Thread.currentThread().getName());
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        contextMessages.add(responseMessage);
        return parseResponseWithValidate(responseMessage);
    }

    @Override
    public String buildResultFormat() throws Exception {
        if (isBaseSupportType(wrappedType))
            return ((BaseSupportType) wrappedType.getConstructor().newInstance()).getFormat();
        else return wrappedType.getSimpleName();
    }

    @Override
    public String createPrompt(String description, String refTypes, String functionTemplate, String constraints, String inputFormat, String resultFormat) {

        return PROMPT_TEMPLATE.formatted(refTypes, description, functionTemplate, constraints, inputFormat, """
                {
                    "key" : %s,
                }
                """.formatted(resultFormat));
    }

    @Override
    public String createFunction(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, "Map<String, " + wrappedType.getSimpleName() + ">");
    }
}
