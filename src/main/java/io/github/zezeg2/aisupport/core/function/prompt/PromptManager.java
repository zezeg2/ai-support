package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.enums.Role;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.bill.Bill;
import io.github.zezeg2.aisupport.common.util.JsonUtil;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.PromptContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * The PromptManager class is responsible for managing prompts and exchanging messages in a chat-based AI system.
 */
@Getter
@RequiredArgsConstructor
public class PromptManager {

    private final OpenAiService service;
    private final PromptContextHolder contextHolder;
    private final ContextProperties contextProperties;

    /**
     * Adds a message to the prompt context.
     *
     * @param messageContext Message context for calling openai chat completion api
     * @param role           The role of the chat message (e.g., user, assistant).
     * @param message        The content of the chat message.
     * @param contextType    The type of context (prompt or feedback).
     */
    public void addMessageToContext(ContextType contextType, MessageContext messageContext, Role role, String message) {
        messageContext.getMessages().add(new ChatMessage(role.getValue(), message));
        contextHolder.saveMessageContext(contextType, messageContext);
    }

    /**
     * Exchange messages in the chat-based AI system and retrieve the chat completion result.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext Message context for calling openai chat completion api
     * @param model          The AI model to use for the chat completion.
     * @param topP           The top-p value for generating diverse completions.
     * @param save           Specifies whether to save the generated response in the prompt context.
     * @return The updated chat context contains chat completion result.
     */

    public <T extends MessageContext> T exchangeMessages(ContextType contextType, MessageContext messageContext, AIModel model, double topP, boolean save) {
        ChatCompletionResult response = createChatCompletion(model, messageContext.getMessages(), topP);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        responseMessage.setContent(JsonUtil.extractJsonFromMessage(responseMessage.getContent()));
        messageContext.setUsage(response.getUsage());
        messageContext.getMessages().add(responseMessage);
        if (save) contextHolder.saveMessageContext(contextType, messageContext);
        return (T) messageContext;
    }

    /**
     * Creates a chat completion request using the AI model and chat messages.
     *
     * @param model    The AI model to use for the chat completion.
     * @param messages The list of chat messages.
     * @param topP     The top-p value for generating diverse completions.
     * @return The chat completion result.
     */
    protected ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages, double topP) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .topP(topP)
                .build());
    }

    public Bill getExecutionBill(PromptMessageContext messageContext) {
        Bill bill = new Bill();
        bill.addUsage(messageContext.getModel(), messageContext.getUsage());
        for (FeedbackMessageContext feedbackMessageContext : messageContext.getFeedbackMessageContexts()) {
            bill.addUsage(feedbackMessageContext.getModel(), feedbackMessageContext.getUsage());
        }
        return bill;
    }
}

