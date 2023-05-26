package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AIMapFunction<T> extends BaseAIFunction<Map<String, T>> {
    private final Class<T> wrappedType;

    public AIMapFunction(String functionName, String purpose, List<Constraint> constraints, Class<Map<String, T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, BuildFormatUtil formatUtil, Class<T> wrappedType) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, formatUtil);
        this.wrappedType = wrappedType;
    }

    public AIMapFunction(String functionName, String purpose, List<Constraint> constraints, Class<Map<String, T>> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, BuildFormatUtil formatUtil, PromptManager promptManager, ResultValidatorChain resultValidatorChain, Class<T> wrappedType) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, formatUtil, promptManager, resultValidatorChain);
        this.wrappedType = wrappedType;
    }

    @Override
    public String buildResultFormat() throws Exception {
        if (isBaseSupportType(wrappedType))
            return ((BaseSupportType) wrappedType.getConstructor().newInstance()).getFormat();
        else return wrappedType.getSimpleName();
    }

    @Override
    public String createPrompt(String description, String refTypes, String functionTemplate, String constraints, String inputFormat, String resultFormat) {

        return PROMPT_TEMPLATE.formatted(description, refTypes, functionTemplate, constraints, inputFormat, """
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
