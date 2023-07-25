package io.github.zezeg2.aisupport.core.function.prompt;

/**
 * Enum representing different types of contexts used in a chat-based AI system.
 * Each context type is associated with a specific class that implements the MessageContext interface.
 */
public enum ContextType {

    /**
     * The PROMPT context type associated with the PromptMessageContext class.
     * This context is used for managing prompts in the AI system.
     *
     * @see PromptMessageContext
     */
    PROMPT(PromptMessageContext.class),

    /**
     * The FEEDBACK context type associated with the FeedbackMessageContext class.
     * This context is used for managing feedback messages in the AI system.
     *
     * @see FeedbackMessageContext
     */
    FEEDBACK(FeedbackMessageContext.class);

    /**
     * The class implementing the MessageContext interface associated with this context type.
     */
    private final Class<? extends MessageContext> contextClass;

    /**
     * Constructor for creating a ContextType enum constant with the specified context class.
     *
     * @param contextClass The class implementing the MessageContext interface for this context type.
     */
    ContextType(Class<? extends MessageContext> contextClass) {
        this.contextClass = contextClass;
    }

    /**
     * Get the class implementing the MessageContext interface for this context type.
     *
     * @return The class implementing the MessageContext interface.
     */
    public Class<? extends MessageContext> getContextClass() {
        return contextClass;
    }
}


