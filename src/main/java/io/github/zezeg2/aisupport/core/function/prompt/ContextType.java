package io.github.zezeg2.aisupport.core.function.prompt;

/**
 * The ContextType enum represents the types of contexts.
 * It includes the prompt context and the feedback context.
 *
 * @since 1.0
 */
public enum ContextType {
    /**
     * The prompt context
     */
    PROMPT(PromptMessageContext.class),

    /**
     * The feedback context
     */
    FEEDBACK(FeedbackMessageContext.class);
    private final Class<? extends MessageContext> contextClass;

    ContextType(Class<? extends MessageContext> contextClass) {
        this.contextClass = contextClass;
    }

    public Class<? extends MessageContext> getContextClass() {
        return contextClass;
    }

}

