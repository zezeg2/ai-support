package io.github.zezeg2.aisupport.config.properties;

/**
 * The MODEL enum represents different models used in the application.
 * It includes various versions of GPT-3.5 and GPT-4, each with specific characteristics and context lengths.
 * Additionally, it contains a special value NONE, which should not be used for properties setting and serves a specific purpose within the application.
 */
public enum MODEL {
    GPT_3_5_TURBO,
    GPT_3_5_TURBO_0301,
    GPT_3_5_TURBO_16_K,
    GPT_3_5_TURBO_0613,
    GPT_3_5_TURBO_16_K_0613,
    GPT_4,
    GPT_4_0314,
    GPT_4_32_K,
    GPT_4_32_K_0314,
    GPT_4_0613,
    GPT_4_32_K_0613,

    /**
     * This special value NONE should not be used in properties setting.
     * It serves a specific purpose within the application.
     */
    NONE
}
