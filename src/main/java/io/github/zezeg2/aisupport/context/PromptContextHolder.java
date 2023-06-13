package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;

public interface PromptContextHolder {

    boolean contains(String namespace);

    void savePrompt(String namespace, Prompt prompt);

    Prompt get(String namespace);

    PromptMessages getPromptChatMessages(String namespace, String identifier);

    FeedbackMessages getFeedbackChatMessages(String namespace, String identifier);

    void savePromptMessages(String namespace, String identifier, ChatMessage message);

    void saveFeedbackMessages(String namespace, String identifier, ChatMessage message);
}
