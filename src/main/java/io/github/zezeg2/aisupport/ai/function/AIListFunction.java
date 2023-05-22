package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;
import java.util.stream.Collectors;

public class AIListFunction<T> extends BaseAIFunction<List<T>> {
    public AIListFunction(String functionName, String description, List<Constraint> constraintList, Class<List<T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, Class<T> wrappedType) {
        super(functionName, description, constraintList, returnType, service, mapper, resolver);
        this.wrappedType = wrappedType;
    }

    private final Class<T> wrappedType;

    @Override
    public List<T> execute(List<Argument<?>> args, AIModel model) throws Exception {
        List<ChatMessage> messages = createMessages(args);
        ChatCompletionResult response = createChatCompletion(model, messages);
        return parseResponse(response);
    }

    @Override
    public String buildResultFormat() throws Exception {
        if (isBaseSupportType(wrappedType))
            return ((BaseSupportType) wrappedType.getConstructor().newInstance()).getFormat();
        else return wrappedType.getSimpleName();
    }

    @Override
    public String createTemplate(String refTypes, String description, String functionTemplate, String constraints, String inputFormat, String resultFormat) {

        return WHOLE_TEMPLATE.formatted(refTypes, description, functionTemplate, constraints, inputFormat, """
                [
                    %s,
                ]
                """.formatted(resultFormat));
    }

    @Override
    public String createFunctionTemplate(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, "List<" + wrappedType.getSimpleName() + ">");
    }
}
