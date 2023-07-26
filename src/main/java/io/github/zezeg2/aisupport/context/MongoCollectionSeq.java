package io.github.zezeg2.aisupport.context;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a document in the "database_sequences" collection in MongoDB.
 * This class is used to store sequence information by identifier
 */
@Data
@Document(collection = "database_sequences")
public class MongoCollectionSeq {

    /**
     * The name of the sequence.
     */
    @Id
    private String seqName;

    /**
     * The identifier of document.
     */
    private String identifier;

    /**
     * The current value of the sequence.
     */
    private long seq;
}
