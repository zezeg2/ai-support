package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ReactivePromptContextHolder {

    Mono<Boolean> contains(String functionName);

    Mono<Void> savePrompt(String functionName, Prompt prompt);

    Mono<Prompt> get(String functionName);

    Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String functionName);

    Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String validatorName);

    Mono<List<ChatMessage>> getPromptChatMessages(String functionName, String identifier);

    Mono<List<ChatMessage>> getFeedbackChatMessages(String validatorName, String identifier);

    Mono<Void> savePromptMessagesContext(String functionName, String identifier, ChatMessage message);

    Mono<Void> saveFeedbackMessagesContext(String validatorName, String identifier, ChatMessage message);
}
