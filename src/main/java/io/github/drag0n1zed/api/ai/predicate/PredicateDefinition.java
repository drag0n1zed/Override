package io.github.drag0n1zed.api.ai.predicate;

/**
 * A complete definition for a targeting predicate.
 *
 * @param identifier A unique string key for this predicate (e.g., "override:is_player_name").
 * @param factory    The {@link PredicateFactory} to instantiate the predicate logic.
 */
public record PredicateDefinition(
        String identifier,
        PredicateFactory factory
) {}