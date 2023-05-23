package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;
import java.util.stream.Collectors;

public class AISingleFunction<T> extends BaseAIFunction<T> {
    public AISingleFunction(String functionName, String description, List<Constraint> constraintList, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver) {
        super(functionName, description, constraintList, returnType, service, mapper, resolver);
    }

    @Override
    public T execute(List<Argument<?>> args, AIModel model) throws Exception {
        List<ChatMessage> messages = createMessages(args);
        ChatCompletionResult response = createChatCompletion(model, messages);
        return parseResponse(response);
    }

    @Override
    public T executeWithContext(List<Argument<?>> args, AIModel model) throws Exception {
        String promptKey = initIfEmptyContext(args);
        addMessageToContext(ROLE.USER, createValuesString(args), promptKey);
        List<ChatMessage> contextMessages = promptMessageContext.get(promptKey);
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        contextMessages.add(responseMessage);
        return parseResponseWithValidate(responseMessage, promptKey);
    }

    @Override
    public String buildResultFormat() throws Exception {
        if (isBaseSupportType(returnType))
            return ((BaseSupportType) returnType.getConstructor().newInstance()).getFormat();
        else return returnType.getSimpleName();
    }

    @Override
    public String createPrompt(String refTypes, String description, String functionTemplate, String constraints, String inputFormat, String resultFormat) {
        return PROMPT_TEMPLATE.formatted(refTypes, description, functionTemplate, constraints, inputFormat, resultFormat);
    }

    @Override
    public String createFunctionTemplate(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }
}
