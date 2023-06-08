package io.github.zezeg2.aisupport.ai.function.prompt;

import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.reactive.ReactiveContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import org.springframework.security.core.Authentication;

public class ReactiveSecurityContextPromptManager extends ReactivePromptManager<Authentication> {
    public ReactiveSecurityContextPromptManager(OpenAiService service, ReactivePromptContextHolder<Authentication> context, ReactiveContextIdentifierProvider<Authentication> identifierProvider, ContextProperties contextProperties) {
        super(service, context, identifierProvider, contextProperties);
    }
}
