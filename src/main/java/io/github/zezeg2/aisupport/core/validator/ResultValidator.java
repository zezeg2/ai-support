package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.util.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.config.properties.MODEL;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
public abstract class ResultValidator {
    protected final int MAX_ATTEMPTS = 3;
    protected String role = null;
    protected final PromptManager promptManager;
    protected final ObjectMapper mapper;
    protected final OpenAIProperties openAIProperties;

    public ResultValidator(PromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        this.promptManager = promptManager;
        this.mapper = mapper;
        this.openAIProperties = openAIProperties;
    }

    protected String getNamespace(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName()));
    }

    protected void init(String functionName, String identifier) {
        FeedbackMessageContext feedbackChatMessages = promptManager.getContextHolder().getContext(ContextType.FEEDBACK, getNamespace(functionName), identifier);
        if (feedbackChatMessages.getMessages().isEmpty()) {
            promptManager.addMessageToContext(getNamespace(functionName), identifier, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
        }
    }

    private String buildTemplate(String functionName) {
        return this.role == null ? TemplateConstants.FEEDBACK_FRAME.formatted(addTemplateContents(functionName), getPrompt(functionName).getResultFormat(), BuildFormatUtil.getFormatString(FeedbackResponse.class)) :
                TemplateConstants.FEEDBACK_FRAME_WITH_ROLE.formatted(this.role, addTemplateContents(functionName), getPrompt(functionName).getResultFormat(), BuildFormatUtil.getFormatString(FeedbackResponse.class));
    }

    public String validate(String functionName, String identifier) {
        MODEL annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
        AIModel model = annotatedModel.equals(MODEL.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
        if (!ignoreCondition(functionName, identifier)) return validate(functionName, identifier, model);
        return getLastPromptResponseContent(functionName, identifier);
    }

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

    protected String exchangeMessages(String functionName, String identifier, String message, ContextType contextType, AIModel model) {
        promptManager.addMessageToContext(functionName, identifier, ROLE.USER, message, contextType);
        double topP = contextType == ContextType.PROMPT ? promptManager.getContextHolder().get(functionName).getTopP()
                : this.getClass().getAnnotation(ValidateTarget.class).topP();
        ChatMessage responseMessage = promptManager.exchangeMessages(contextType, functionName, identifier, model, topP, true).getChoices().get(0).getMessage();
        return responseMessage.getContent();
    }

    protected String getLastPromptResponseContent(String functionName, String identifier) {
        List<ChatMessage> promptMessageList = promptManager.getContextHolder().getContext(ContextType.PROMPT, functionName, identifier).getMessages();
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    protected abstract String addTemplateContents(String functionName);

    protected Prompt getPrompt(String functionName) {
        return promptManager.getContextHolder().get(functionName);
    }

    protected boolean ignoreCondition(String functionName, String identifier) {
        return false;
    }
}
