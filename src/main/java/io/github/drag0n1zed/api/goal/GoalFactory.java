package io.github.drag0n1zed.api.goal;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * A factory for creating instances of AI {@link Goal}s.
 * <p>
 * Implement this functional interface to provide a constructor for a specific
 * goal type, allowing it to be deserialized and applied to a {@link Mob}.
 */
@FunctionalInterface
public interface GoalFactory {

    /**
     * Creates a new Goal instance.
     *
     * @param mob The mob instance to which the goal will be applied.
     * @param data A CompoundTag containing any additional parameters for this goal.
     *             This is reserved for future use and may be empty.
     * @return A new, configured {@link Goal} instance.
     */
    Goal create(Mob mob, CompoundTag data);
}