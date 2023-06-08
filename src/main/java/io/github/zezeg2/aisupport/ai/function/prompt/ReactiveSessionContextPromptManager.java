package io.github.zezeg2.aisupport.ai.function.prompt;

import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.reactive.ReactiveContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import org.springframework.web.server.ServerWebExchange;

public class ReactiveSessionContextPromptManager extends ReactivePromptManager<ServerWebExchange> {
    public ReactiveSessionContextPromptManager(OpenAiService service, ReactivePromptContextHolder<ServerWebExchange> context, ReactiveContextIdentifierProvider<ServerWebExchange> identifierProvider, ContextProperties contextProperties) {
        super(service, context, identifierProvider, contextProperties);
    }
}
