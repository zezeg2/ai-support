package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;
import java.util.stream.Collectors;

public class AISingleFunction<T> extends BaseAIFunction<T> {
    public AISingleFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, BuildFormatUtil formatUtil, PromptManager promptManager, ResultValidatorChain resultValidatorChain, OpenAIProperties openAIProperties) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, formatUtil, promptManager, resultValidatorChain, openAIProperties);
    }

    @Override
    public String buildResultFormat() {
        return formatUtil.getFormatString(returnType);
    }

    @Override
    public String createFunction(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }
}
