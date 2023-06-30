package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.config.properties.MODEL;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
public abstract class ResultValidator {
    protected static final int MAX_ATTEMPTS = 3;
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
        FeedbackMessages feedbackChatMessages = promptManager.getContext().getFeedbackChatMessages(getNamespace(functionName), identifier);
        if (feedbackChatMessages.getContent().isEmpty()) {
            promptManager.addMessage(getNamespace(functionName), identifier, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
        }
    }

    private String buildTemplate(String functionName) {
        return TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), addTemplateContents(functionName));
    }

    public String validate(String identifier, String functionName) {
        MODEL annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
        AIModel model = annotatedModel.equals(MODEL.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
        return validate(identifier, functionName, model);
    }

    public String validate(String identifier, String functionName, AIModel model) {
        init(functionName, identifier);
        String lastResponseContent;
        String lastFeedbackContent;
        lastResponseContent = getLastPromptResponseContent(functionName, identifier);

        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + " ---------------------------------------------------------------------------\n" + lastResponseContent);
            lastFeedbackContent = exchangeMessages(getNamespace(functionName), identifier, lastResponseContent, ContextType.FEEDBACK, model);
            FeedbackResponse feedbackResult;
            try {
                feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
            } catch (JsonProcessingException e) {
                promptManager.getContext().deleteLastFeedbackMessage(getNamespace(functionName), identifier, 2);
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
        promptManager.addMessage(functionName, identifier, ROLE.USER, message, contextType);
        ChatMessage responseMessage = switch (contextType) {
            case PROMPT -> {
                double topP = promptManager.getContext().get(functionName).getTopP();
                yield promptManager.exchangePromptMessages(functionName, identifier, model, topP, true).getChoices().get(0).getMessage();
            }
            case FEEDBACK -> {
                double topP = this.getClass().getAnnotation(ValidateTarget.class).topP();
                yield promptManager.exchangeFeedbackMessages(functionName, identifier, model, topP, true).getChoices().get(0).getMessage();
            }
        };
        return responseMessage.getContent();
    }

    protected String getLastPromptResponseContent(String functionName, String identifier) {
        List<ChatMessage> promptMessageList = promptManager.getContext().getPromptChatMessages(functionName, identifier).getContent();
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    protected abstract String addTemplateContents(String functionName);

    protected Prompt getPrompt(String functionName) {
        return promptManager.getContext().get(functionName);
    }
}
