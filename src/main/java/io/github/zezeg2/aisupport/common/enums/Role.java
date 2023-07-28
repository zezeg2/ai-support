package io.github.zezeg2.aisupport.common.enums;

import lombok.Getter;

/**
 * The ROLE enumeration represents the roles of ChatMessage exchanged when using OpenAI's ChatCompletion API.
 */
@Getter
public enum Role {
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
    Role(String value) {
        this.value = value;
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
