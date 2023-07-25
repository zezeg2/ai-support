package io.github.zezeg2.aisupport.common.enums;

/**
 * The ROLE enumeration represents the roles of ChatMessage exchanged when using OpenAI's ChatCompletion API.
 */
public enum ROLE {
    /**
     * The SYSTEM role represents a system-level entity in the conversation.
     */
    SYSTEM("system"),

    /**
     * The USER role represents a user or human entity in the conversation.
     */
    USER("user"),

    /**
     * The ASSISTANT role represents an assistant or AI entity in the conversation.
     */
    ASSISTANT("assistant");

    private final String value;

    /**
     * Constructor for the ROLE enumeration.
     *
     * @param value The string value associated with the role.
     */
    ROLE(String value) {
        this.value = value;
    }

    /**
     * Retrieves the string value associated with the role.
     *
     * @return The string value of the role.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the string representation of the role (same as its value).
     *
     * @return The string representation of the role.
     */
    @Override
    public String toString() {
        return this.value;
    }
}
