package io.github.zezeg2.aisupport.core.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;
import io.github.zezeg2.aisupport.core.validator.DefaultResultValidatorChain;

import java.util.List;

public class DefaultAIFunction<T> extends BaseAIFunction<T, DefaultPromptManager, DefaultResultValidatorChain> {
    public DefaultAIFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, DefaultPromptManager promptManager, DefaultResultValidatorChain resultValidatorChain, OpenAIProperties openAIProperties) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, promptManager, resultValidatorChain, openAIProperties);
    }

    @Override
    protected T parseResponseWithValidate(ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        content = resultValidatorChain.validate(functionName, content);
        try {
            return mapper.readValue(content, returnType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String buildResultFormat() {
        return BuildFormatUtil.getFormatString(returnType);
    }

    @Override
    public T execute(List<Argument<?>> args) {
        AIModel model = getDefaultModel();
        return execute(args, model);
    }

    @Override
    public T execute(List<Argument<?>> args, AIModel model) {
        init(args);
        ChatCompletionResult response = promptManager.exchangePromptMessages(functionName, model, true);
        return parseResponseWithValidate(response);
    }

    @Override
    protected String setReturnType() {
        return returnType.getSimpleName();
    }
}
