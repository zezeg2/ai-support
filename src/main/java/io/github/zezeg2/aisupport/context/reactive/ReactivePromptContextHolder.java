package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import reactor.core.publisher.Mono;

public interface ReactivePromptContextHolder {

    Mono<Boolean> contains(String namespace);

    Mono<Void> savePrompt(String namespace, Prompt prompt);

    Mono<Prompt> get(String namespace);

    Mono<PromptMessages> getPromptChatMessages(String namespace, String identifier);

    Mono<FeedbackMessages> getFeedbackChatMessages(String namespace, String identifier);

    Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message);

    Mono<Void> saveFeedbackMessages(String namespace, String identifier, ChatMessage message);
}
