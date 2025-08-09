package io.github.drag0n1zed.api.ai.predicate;

import java.util.List;
import java.util.Optional;

/**
 * The public interface for the Override AI Predicate Registry.
 * Allows for registration of logical predicates used in configurable AI goals.
 */
public interface IPredicateRegistry {

    /**
     * Registers a complete predicate definition.
     *
     * @param definition The predicate definition to register.
     */
    void register(PredicateDefinition definition);

    /**
     * Retrieves a predicate definition by its unique identifier.
     *
     * @param identifier The predicate's unique identifier.
     * @return An Optional containing the definition if found, otherwise empty.
     */
    Optional<PredicateDefinition> getDefinition(String identifier);

    /**
     * @return An unmodifiable list of all registered predicate definitions.
     */
    List<PredicateDefinition> getAllDefinitions();
}