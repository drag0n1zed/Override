package io.github.drag0n1zed.api.ai;

import io.github.drag0n1zed.api.ai.validation.ValidationResult;
import java.util.function.Function;
import net.minecraft.world.entity.Mob;
import io.github.drag0n1zed.api.goal.GoalFactory;
/**
 * A complete definition for a custom AI goal.
 * <p>
 * This record encapsulates all the necessary components for managing a goal:
 * its unique identifier, the logic to validate its compatibility with a mob,
 * and the factory to create an instance of the goal.
 *
 * @param identifier A unique string key for this goal (e.g., "minecraft:swell").
 * @param validator  A function that checks if a mob is capable of executing this goal
 *                   and returns a {@link ValidationResult}.
 * @param factory    The {@link GoalFactory} to instantiate the goal.
 */
public record GoalDefinition(
        String identifier,
        Function<Mob, ValidationResult> validator,
        GoalFactory factory
) {}