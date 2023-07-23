package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public interface MessageContext {
    String getFunctionName();

    String getIdentifier();

    List<ChatMessage> getMessages();

    void setFunctionName(String functionName);

    void setIdentifier(String identifier);

    void setMessages(List<ChatMessage> messages);
}
