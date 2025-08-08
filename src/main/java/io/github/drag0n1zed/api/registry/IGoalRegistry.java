package io.github.drag0n1zed.api.registry;

import io.github.drag0n1zed.api.goal.GoalFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * The internal interface for the goal factory registry.
 * The core Override mod provides an implementation of this.
 */
@ApiStatus.Internal
public interface IGoalRegistry {
    /**
     * Registers a factory for a given goal name.
     * @param goalName The unique name of the goal.
     * @param factory The factory that creates the goal.
     */
    void register(String goalName, GoalFactory factory);

    /**
     * Retrieves a factory for a given goal name.
     * @param goalName The name of the goal to retrieve.
     * @return The corresponding GoalFactory, or null if not found.
     */
    @Nullable
    GoalFactory get(String goalName);
}