package io.github.drag0n1zed.api;

import io.github.drag0n1zed.api.goal.GoalFactory;
import io.github.drag0n1zed.api.registry.IGoalRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Provides the central registration points for the Override API.
 * This class is the primary entry point for other mods ("plugins") to
 * add support for their own custom AI goals.
 */
public final class OverrideRegistries {
    @Nullable
    private static IGoalRegistry registryInstance = null;

    private OverrideRegistries() {}

    /**
     * Registers a factory for a custom AI goal.
     * <p>
     * This method should be called during your mod's initialization phase.
     *
     * @param goalName The unique identifier for this goal (e.g., "MyAwesomeGoal").
     * @param factory  The factory implementation that can create an instance of this goal.
     * @throws IllegalStateException if called before the Override mod has initialized its API.
     */
    public static void registerGoalFactory(String goalName, GoalFactory factory) {
        if (registryInstance == null) {
            throw new IllegalStateException("Cannot register goal factory. The Override mod's API registry has not been initialized yet.");
        }
        registryInstance.register(goalName, factory);
    }

    /**
     * FOR INTERNAL USE BY THE OVERRIDE MOD ONLY.
     * This method is called by the core mod to provide its registry implementation to the API.
     *
     * @param registry The implementation of the registry from the core mod.
     */
    @ApiStatus.Internal
    public static void setRegistryProvider(IGoalRegistry registry) {
        if (registryInstance != null) {
            throw new IllegalStateException("The Override API registry provider has already been set.");
        }
        registryInstance = registry;
    }

    /**
     * FOR INTERNAL USE BY THE OVERRIDE MOD ONLY.
     * Retrieves the registry implementation.
     *
     * @return The IGoalRegistry implementation, or null if not initialized.
     */
    @ApiStatus.Internal
    @Nullable
    public static IGoalRegistry getRegistryProvider() {
        return registryInstance;
    }
}