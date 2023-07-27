package io.github.zezeg2.aisupport.core.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessageContext;

import java.util.Arrays;
import java.util.List;

/**
 * The ResultValidatorChain class represents a chain of ResultValidator instances that are used to validate AI model results in a chat-based AI system.
 * It allows chaining multiple ResultValidator instances together and performing validation based on specified target functions.
 */
public class ResultValidatorChain {

    /**
     * The list of ResultValidator instances forming the chain.
     */
    protected final List<ResultValidator> validators;

    /**
     * Constructs a ResultValidatorChain with the given list of validators.
     *
     * @param validators The list of ResultValidator instances to be used in the chain.
     */
    public ResultValidatorChain(List<ResultValidator> validators) {
        this.validators = validators;
    }

    /**
     * Validates the AI model results for the specified function and identifier using the validators in the chain.
     * The validation is performed based on the target function's annotations defined in the validators.
     * If a target function is marked as "global" or is explicitly listed in the validator's target names,
     * the corresponding validator is used to validate the results.
     *
     * @param promptMessageContext Prompt message context for calling openai chat completion api
     * @return The validated result of all validator chain(all validators) as a string.
     */
    public String validate(PromptMessageContext promptMessageContext) {
        List<ChatMessage> messages = promptMessageContext.getMessages();
        String result = messages.get(messages.size() - 1).getContent();
        for (ResultValidator validator : validators) {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            if (targetFunction.global() || targetFunctionList.contains(promptMessageContext.getFunctionName())) {
                result = validator.validate(promptMessageContext);
            }
        }
        return result;
    }

    /**
     * Retrieves the list of ResultValidator instances that are applicable to the specified function name.
     * The applicable validators are filtered based on the target function's annotations defined in the validators.
     * If a target function is marked as "global" or is explicitly listed in the validator's target names,
     * the corresponding validator is included in the returned list.
     *
     * @param functionName The name of the function for which to retrieve applicable validators.
     * @return The list of applicable ResultValidator instances.
     */
    public List<ResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
