package io.github.drag0n1zed.api.goal;

import io.github.drag0n1zed.api.ai.GoalDefinition;
import io.github.drag0n1zed.api.ai.validation.ValidationResult;
import io.github.drag0n1zed.ai.AIGoalData;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.entity.Mob;

/**
 * The public interface for the Override AI Goal Registry.
 * Allows for registration of goal definitions and querying for compatibility.
 */
public interface IGoalRegistry {

    /**
     * Registers a complete goal definition.
     *
     * @param definition The goal definition to register.
     */
    void register(GoalDefinition definition);

    /**
     * Retrieves a goal definition by its unique identifier.
     *
     * @param identifier The goal's unique identifier.
     * @return An Optional containing the definition if found, otherwise empty.
     */
    Optional<GoalDefinition> getDefinition(String identifier);

    /**
     * @return An unmodifiable list of all registered goal definitions.
     */
    List<GoalDefinition> getAllDefinitions();

    /**
     * Filters all registered goals and returns a list of definitions that
     * are compatible with the given mob.
     *
     * @param mob The mob to check for compatibility.
     * @return A list of compatible goal definitions.
     */
    List<GoalDefinition> getCompatibleDefinitions(Mob mob);

    /**
     * Validates a proposed AI profile against a given mob.
     *
     * @param mob     The mob to validate against.
     * @param profile The list of {@link AIGoalData} to validate.
     * @return A map where each goal data is mapped to its validation result.
     *         Goals that pass validation will map to a successful result.
     */
    Map<AIGoalData, ValidationResult> validateProfile(Mob mob, List<AIGoalData> profile);
}