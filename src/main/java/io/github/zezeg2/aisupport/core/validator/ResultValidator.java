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
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
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
    protected void init(String functionName, String identifier) {
        FeedbackMessageContext feedbackChatMessages = promptManager.getContextHolder().getContext(ContextType.FEEDBACK, getNamespace(functionName), identifier);
        if (feedbackChatMessages.getMessages().isEmpty()) {
            promptManager.addMessageToContext(getNamespace(functionName), identifier, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
        }
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
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return The validated result as a string.
     */
    public String validate(String functionName, String identifier) {
        MODEL annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
        AIModel model = annotatedModel.equals(MODEL.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
        if (!ignoreCondition(functionName, identifier)) return validate(functionName, identifier, model);
        return getLastPromptResponseContent(functionName, identifier);
    }

    /**
     * Validates the AI model results for the specified function, identifier, and AI model.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @param model        The AI model to use for validation.
     * @return The validated result as a string.
     */
    public String validate(String functionName, String identifier, AIModel model) {
        String lastFeedbackContent;
        String lastResponseContent = getLastPromptResponseContent(functionName, identifier);
        init(functionName, identifier);

        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + " ---------------------------------------------------------------------------\n" + lastResponseContent);
            lastFeedbackContent = exchangeMessages(getNamespace(functionName), identifier, lastResponseContent, ContextType.FEEDBACK, model);
            FeedbackResponse feedbackResult;
            try {
                feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
            } catch (JsonProcessingException e) {
                promptManager.getContextHolder().deleteMessagesFromLast(ContextType.FEEDBACK, getNamespace(functionName), identifier, 2);
                continue;
            }

            if (feedbackResult.isValid()) {
                return lastResponseContent;
            }
            System.out.println("Feedback on results exists\n" + lastFeedbackContent);
            lastResponseContent = exchangeMessages(functionName, identifier, lastFeedbackContent, ContextType.PROMPT, model);
        }

        throw new RuntimeException("Maximum Validate count over");
    }

    /**
     * Exchanges messages with the AI model and retrieves the response content.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @param message      The content of the chat message.
     * @param contextType  The type of context (prompt or feedback).
     * @param model        The AI model to use for the chat completion.
     * @return The content of the AI model's response as a string.
     */

    protected String exchangeMessages(String functionName, String identifier, String message, ContextType contextType, AIModel model) {
        promptManager.addMessageToContext(functionName, identifier, ROLE.USER, message, contextType);
        double topP = contextType == ContextType.PROMPT ? promptManager.getContextHolder().get(functionName).getTopP()
                : this.getClass().getAnnotation(ValidateTarget.class).topP();
        ChatMessage responseMessage = promptManager.exchangeMessages(contextType, functionName, identifier, model, topP, true).getChoices().get(0).getMessage();
        return responseMessage.getContent();
    }

    /**
     * Gets the content of the last prompt response from the chat context.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return The content of the last prompt response as a string.
     */
    protected String getLastPromptResponseContent(String functionName, String identifier) {
        List<ChatMessage> promptMessageList = promptManager.getContextHolder().getContext(ContextType.PROMPT, functionName, identifier).getMessages();
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
     * Determines whether the validation should be ignored based on the function and identifier.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return true if the validation should be ignored, false otherwise.
     */
    protected boolean ignoreCondition(String functionName, String identifier) {
        return false;
    }
}
