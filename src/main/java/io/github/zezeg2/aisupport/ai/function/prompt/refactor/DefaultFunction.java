package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;

public class DefaultFunction<T> extends BaseFunction<T, DefaultPromptManager, DefaultResultValidatorChain> {
    public DefaultFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, DefaultPromptManager promptManager, DefaultResultValidatorChain resultValidatorChain, ExceptionValidator exceptionValidator, OpenAIProperties openAIProperties) {
        super(functionName, purpose, constraints, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
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
