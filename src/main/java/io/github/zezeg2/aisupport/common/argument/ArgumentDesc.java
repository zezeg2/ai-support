package io.github.zezeg2.aisupport.common.argument;

import lombok.Builder;
import lombok.Data;

/**
 * The ArgumentDesc class represents the description of an argument, consisting of a key description and a value description.
 */
@Data
@Builder
public class ArgumentDesc {
    /**
     * The description of the key associated with the argument.
     */
    private String keyDesc;

    /**
     * The description of the value associated with the argument.
     */
    private String valueDesc;
}
