package io.github.drag0n1zed.api.ai.validation;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class providing common, pre-built validator functions for use
 * in {@link io.github.drag0n1zed.api.ai.GoalDefinition}s.
 */
public final class GoalValidators {

    private GoalValidators() {}

    /**
     * A validator that always returns success. Use for goals with no special requirements.
     */
    public static Function<Mob, ValidationResult> alwaysTrue() {
        return mob -> ValidationResult.success();
    }

    /**
     * Creates a validator that checks if the mob is an instance of a specific class or interface.
     *
     * @param requiredType The required class or interface.
     * @return A validator function.
     */
    public static Function<Mob, ValidationResult> requiresClass(Class<?> requiredType) {
        return mob -> {
            if (requiredType.isInstance(mob)) {
                return ValidationResult.success();
            }
            return ValidationResult.failure("Requires mob to be of type: " + requiredType.getSimpleName());
        };
    }

    /**
     * Creates a validator that checks if the mob possesses a specific attribute.
     *
     * @param attribute The required attribute.
     * @return A validator function.
     */
    public static Function<Mob, ValidationResult> requiresAttribute(Attribute attribute) {
        return mob -> {
            if (mob.getAttributes().hasAttribute(Holder.direct(attribute))) {
                return ValidationResult.success();
            }
            String attributeName = attribute.toString();
            return ValidationResult.failure("Missing required attribute: " + attributeName);
        };
    }

    /**
     * Creates a validator that checks if the mob has a specific type of navigation component.
     * For example, requiring GroundPathNavigation for door-opening goals.
     *
     * @param navClass The required PathNavigation class.
     * @return A validator function.
     */
    public static Function<Mob, ValidationResult> requiresNavigation(Class<? extends PathNavigation> navClass) {
        return mob -> {
            if (navClass.isInstance(mob.getNavigation())) {
                return ValidationResult.success();
            }
            return ValidationResult.failure("Requires navigation of type: " + navClass.getSimpleName());
        };
    }

    /**
     * Creates a validator that checks if the mob is tameable and tamed.
     * This is an example of a more complex, custom check.
     */
    public static Function<Mob, ValidationResult> requiresTamedHorse() {
        return mob -> {
            if (mob instanceof AbstractHorse horse) {
                if (horse.isTamed()) {
                    return ValidationResult.success();
                }
                return ValidationResult.failure("Horse is not tamed.");
            }
            return ValidationResult.failure("Requires a mob of type AbstractHorse.");
        };
    }

    /**
     * Composes multiple validators into a single validator that succeeds only if all
     * composed validators succeed.
     *
     * @param validators The array of validators to combine.
     * @return A single composite validator function.
     */
    @SafeVarargs
    public static Function<Mob, ValidationResult> allOf(Function<Mob, ValidationResult>... validators) {
        return mob -> {
            List<String> failures = new ArrayList<>();
            for (Function<Mob, ValidationResult> validator : validators) {
                ValidationResult result = validator.apply(mob);
                if (!result.isSuccess()) {
                    failures.addAll(result.getReasons());
                }
            }
            return failures.isEmpty() ? ValidationResult.success() : ValidationResult.failure(failures);
        };
    }
}