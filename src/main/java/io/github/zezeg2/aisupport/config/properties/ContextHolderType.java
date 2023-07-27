package io.github.zezeg2.aisupport.config.properties;

/**
 * This enum represents the available contexts for data storage and retrieval.
 * <p>
 * The contexts include LOCAL, REDIS, and MONGO.
 */
public enum ContextHolderType {

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
