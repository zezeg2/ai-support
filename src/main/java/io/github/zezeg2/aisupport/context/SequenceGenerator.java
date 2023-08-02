package io.github.zezeg2.aisupport.context;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * A utility class for generating sequences using MongoDB as a backend data store.
 * This class is responsible for generating unique identifiers based on the provided sequence name and identifier.
 */
@Component
@RequiredArgsConstructor
public class SequenceGenerator {

    /**
     * The MongoOperations instance used for interacting with MongoDB.
     */
    private final MongoOperations mongoOperations;

    /**
     * Generates and returns the next value in the specified sequence for the given identifier.
     *
     * @param seqName    The name of the sequence to generate from.
     * @param identifier The identifier associated with the sequence.
     * @return The next value in the sequence.
     */
    public long generateSequence(String seqName, String identifier) {
        MongoCollectionSeq counter = mongoOperations.findAndModify(
                query(where("seqName").is(seqName).and("identifier").is(identifier)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true),
                MongoCollectionSeq.class);

        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
}

