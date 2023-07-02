package io.github.zezeg2.aisupport.config.properties;

/**
 * This enum represents the available contexts for data storage and retrieval.
 * <p>
 * The contexts include LOCAL, REDIS, and MONGO.
 *
 * @since 1.0
 */
public enum CONTEXT {

    /**
     * The LOCAL context represents data storage and retrieval on the local machine.
     */
    LOCAL,
    /**
     * The REDIS context represents data storage and retrieval using Redis database.
     */
    REDIS,
    /**
     * The MONGO context represents data storage and retrieval using MongoDB.
     */
    MONGO
}
