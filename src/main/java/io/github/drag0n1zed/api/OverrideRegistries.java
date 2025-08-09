package io.github.drag0n1zed.api;

import io.github.drag0n1zed.api.ai.GoalDefinition;
import io.github.drag0n1zed.api.ai.predicate.IPredicateRegistry;
import io.github.drag0n1zed.api.ai.predicate.PredicateDefinition;
import io.github.drag0n1zed.api.goal.IGoalRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Provides the central registration points for the Override API.
 * This class is the primary entry point for other mods to register custom
 * AI goals and the logical predicates that configure them.
 */
public final class OverrideRegistries {
    @Nullable
    private static IGoalRegistry goalRegistryInstance = null;
    @Nullable
    private static IPredicateRegistry predicateRegistryInstance = null;

    private OverrideRegistries() {}

    /**
     * Registers a complete definition for a custom AI goal.
     *
     * @param definition The goal definition, including validation and creation logic.
     * @throws IllegalStateException if called before the Override mod has initialized its registries.
     */
    public static void registerGoal(GoalDefinition definition) {
        if (goalRegistryInstance == null) {
            throw new IllegalStateException("Cannot register goal. The Override Goal Registry has not been initialized yet.");
        }
        goalRegistryInstance.register(definition);
    }

    /**
     * Registers a complete definition for a custom targeting predicate.
     *
     * @param definition The predicate definition, including its creation logic.
     * @throws IllegalStateException if called before the Override mod has initialized its registries.
     */
    public static void registerPredicate(PredicateDefinition definition) {
        if (predicateRegistryInstance == null) {
            throw new IllegalStateException("Cannot register predicate. The Override Predicate Registry has not been initialized yet.");
        }
        predicateRegistryInstance.register(definition);
    }

    /**
     * FOR INTERNAL USE BY THE OVERRIDE MOD ONLY.
     */
    @ApiStatus.Internal
    public static void setGoalRegistryProvider(IGoalRegistry registry) {
        if (goalRegistryInstance != null) {
            throw new IllegalStateException("The Override Goal Registry provider has already been set.");
        }
        goalRegistryInstance = registry;
    }

    /**
     * FOR INTERNAL USE BY THE OVERRIDE MOD ONLY.
     */
    @ApiStatus.Internal
    @Nullable
    public static IGoalRegistry getGoalRegistryProvider() {
        return goalRegistryInstance;
    }

    /**
     * FOR INTERNAL USE BY THE OVERRIDE MOD ONLY.
     */
    @ApiStatus.Internal
    public static void setPredicateRegistryProvider(IPredicateRegistry registry) {
        if (predicateRegistryInstance != null) {
            throw new IllegalStateException("The Override Predicate Registry provider has already been set.");
        }
        predicateRegistryInstance = registry;
    }

    /**
     * FOR INTERNAL USE BY THE OVERRIDE MOD ONLY.
     */
    @ApiStatus.Internal
    @Nullable
    public static IPredicateRegistry getPredicateRegistryProvider() {
        return predicateRegistryInstance;
    }
}