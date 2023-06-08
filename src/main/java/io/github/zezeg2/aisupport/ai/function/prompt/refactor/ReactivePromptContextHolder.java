package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.theokanning.openai.completion.chat.ChatMessage;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ReactivePromptContextHolder {

    Mono<Boolean> contains(String functionName);

    Mono<Void> savePrompt(String functionName, Prompt prompt);

    Mono<Prompt> get(String functionName);

    Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String functionName);

    Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String functionName, String validatorName);

    Mono<List<ChatMessage>> getPromptChatMessages(String functionName, String identifier);

    Mono<List<ChatMessage>> getFeedbackChatMessages(String functionName, String validatorName, String identifier);

    Mono<Void> savePromptMessagesContext(String functionName, String identifier, ChatMessage message);

    Mono<Void> saveFeedbackMessagesContext(String functionName, String validatorName, String identifier, ChatMessage message);
}
