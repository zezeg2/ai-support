package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ReactivePromptContextHolder {

    Mono<Boolean> contains(String namespace);

    Mono<Void> savePrompt(String namespace, Prompt prompt);

    Mono<Prompt> get(String namespace);

    Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String namespace);

    Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String namespace);

    Mono<List<ChatMessage>> getPromptChatMessages(String namespace, String identifier);

    Mono<List<ChatMessage>> getFeedbackChatMessages(String namespace, String identifier);

    Mono<Void> savePromptMessagesContext(String namespace, String identifier, ChatMessage message);

    Mono<Void> saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message);
}
