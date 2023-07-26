package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.common.util.BuildFormatUtil;
import io.github.zezeg2.aisupport.config.properties.MODEL;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 * The abstract ResultValidator class provides a framework for validating AI model results in a chat-based AI system.
 */
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
public abstract class ResultValidator {
    protected final int MAX_ATTEMPTS = 3;
    protected String role = null;
    protected final PromptManager promptManager;
    protected final ObjectMapper mapper;
    protected final OpenAIProperties openAIProperties;

    /**
     * Constructs a ResultValidator with the necessary dependencies.
     *
     * @param promptManager    The PromptManager instance for managing prompts and messages.
     * @param mapper           The ObjectMapper for JSON serialization and deserialization.
     * @param openAIProperties The properties for configuring the OpenAI service.
     */
    public ResultValidator(PromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        this.promptManager = promptManager;
        this.mapper = mapper;
        this.openAIProperties = openAIProperties;
    }

    /**
     * Gets the namespace for the prompt context based on the function name and the validator's class name.
     *
     * @param functionName The name of the function.
     * @return The namespace for the prompt context.
     */
    protected String getNamespace(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName()));
    }

    /**
     * Initializes the feedback context with a system message containing the feedback template.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     */
    protected FeedbackMessageContext init(String functionName, String identifier) {
        FeedbackMessageContext feedbackMessageContext = promptManager.getContextHolder().createMessageContext(ContextType.FEEDBACK, getNamespace(functionName), identifier);
        promptManager.addMessageToContext(feedbackMessageContext, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
        return feedbackMessageContext;
    }

    /**
     * Builds the feedback template based on the function name and the validator's role (if provided).
     *
     * @param functionName The name of the function.
     * @return The feedback template as a string.
     */
    private String buildTemplate(String functionName) {
        return this.role == null ? TemplateConstants.FEEDBACK_FRAME.formatted(addTemplateContents(functionName), getPrompt(functionName).getResultFormat(), BuildFormatUtil.getFormatString(FeedbackResponse.class)) :
                TemplateConstants.FEEDBACK_FRAME_WITH_ROLE.formatted(this.role, addTemplateContents(functionName), getPrompt(functionName).getResultFormat(), BuildFormatUtil.getFormatString(FeedbackResponse.class));
    }

    /**
     * Validates the AI model results for the specified function and identifier.
     *
     * @param promptMessageContext Prompt message context for calling openai chat completion api
     * @return The validated result as a string.
     */
    public String validate(PromptMessageContext promptMessageContext) {
        MODEL annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
        AIModel model = annotatedModel.equals(MODEL.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
        if (!ignoreCondition(promptMessageContext.getFunctionName(), promptMessageContext.getIdentifier()))
            return validate(promptMessageContext, model);
        return getLastPromptResponseContent(promptMessageContext);
    }

    /**
     * Validates the AI model results for the specified function, identifier, and AI model.
     *
     * @param promptMessageContext Prompt Message context for calling openai chat completion api
     * @param model                The AI model to use for validation.
     * @return The validated result as a string.
     */
    public String validate(PromptMessageContext promptMessageContext, AIModel model) {
        String lastResponseContent = getLastPromptResponseContent(promptMessageContext);
        MessageContext feedbackMessageContext = init(promptMessageContext.getFunctionName(), promptMessageContext.getIdentifier());
        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + " ---------------------------------------------------------------------------\n" + lastResponseContent);
            String lastFeedbackContent = exchangeMessages(feedbackMessageContext, lastResponseContent, ContextType.FEEDBACK, model);
            FeedbackResponse feedbackResult;
            try {
                feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
            } catch (JsonProcessingException e) {
                promptManager.getContextHolder().deleteMessagesFromLast(ContextType.FEEDBACK, feedbackMessageContext, 2);
                continue;
            }

            if (feedbackResult.isValid()) {
                return lastResponseContent;
            }
            System.out.println("Feedback on results exists\n" + lastFeedbackContent);
            lastResponseContent = exchangeMessages(promptMessageContext, lastFeedbackContent, ContextType.PROMPT, model);
        }

        throw new RuntimeException("Maximum Validate count over");
    }

    /**
     * Exchanges messages with the AI model and retrieves the response content.
     *
     * @param messageContext Message context for calling openai chat completion api.
     * @param message        The content of the chat message.
     * @param contextType    The type of context (prompt or feedback).
     * @param model          The AI model to use for the chat completion.
     * @return The content of the AI model's response as a string.
     */
    protected String exchangeMessages(MessageContext messageContext, String message, ContextType contextType, AIModel model) {
        promptManager.addMessageToContext(messageContext, ROLE.USER, message, contextType);
        double topP = contextType == ContextType.PROMPT ? promptManager.getContextHolder().get(messageContext.getFunctionName()).getTopP()
                : this.getClass().getAnnotation(ValidateTarget.class).topP();
        List<ChatMessage> messages = promptManager.exchangeMessages(contextType, messageContext, model, topP, true).getMessages();
        ChatMessage responseMessage = messages.get(messages.size() - 1);
        return responseMessage.getContent();
    }

    /**
     * Gets the content of the last prompt response from the chat context.
     *
     * @param promptMessageContext Prompt message context for calling openai chat completion api
     * @return The content of the last prompt response as a string.
     */
    protected String getLastPromptResponseContent(PromptMessageContext promptMessageContext) {
        List<ChatMessage> promptMessageList = promptMessageContext.getMessages();
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    /**
     * Adds the necessary template contents for feedback.
     *
     * @param functionName The name of the function.
     * @return The template contents for feedback as a string.
     */
    protected abstract String addTemplateContents(String functionName);

    /**
     * Gets the prompt for the specified function.
     *
     * @param functionName The name of the function.
     * @return The prompt associated with the function.
     */
    protected Prompt getPrompt(String functionName) {
        return promptManager.getContextHolder().get(functionName);
    }

    /**
     * Determines whether the validation should be ignored based on prompt or message context information
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return true if the validation should be ignored, false otherwise.
     */
    protected boolean ignoreCondition(String functionName, String identifier) {
        return false;
    }
}
