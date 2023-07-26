package io.github.zezeg2.aisupport.context.reactive;

import io.github.zezeg2.aisupport.context.MongoCollectionSeq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * A utility class for generating sequences using MongoDB as a backend data store in a reactive manner.
 * This class is responsible for generating unique identifiers based on the provided sequence name and identifier.
 */
@Component
@RequiredArgsConstructor
public class ReactiveSequenceGenerator {

    /**
     * The ReactiveMongoOperations instance used for interacting with MongoDB reactively.
     */
    private final ReactiveMongoOperations mongoOperations;

    /**
     * Generates and returns the next value in the specified sequence for the given identifier in a reactive manner.
     *
     * @param seqName    The name of the sequence to generate from.
     * @param identifier The identifier associated with the sequence.
     * @return A Mono emitting the next value in the sequence.
     */
    public Mono<Long> generateSequence(String seqName, String identifier) {
        return mongoOperations.findAndModify(
                        query(where("seqName").is(seqName).and("identifier").is(identifier)),
                        new Update().inc("seq", 1),
                        options().returnNew(true).upsert(true),
                        MongoCollectionSeq.class)
                .map(counter -> !Objects.isNull(counter) ? counter.getSeq() : 1);
    }
}
