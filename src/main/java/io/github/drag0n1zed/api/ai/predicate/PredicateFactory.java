package io.github.drag0n1zed.api.ai.predicate;

import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * A factory for creating a {@link Predicate<LivingEntity>} instance from NBT data.
 * This is used to reconstruct targeting conditions at runtime.
 */
@FunctionalInterface
public interface PredicateFactory {
    /**
     * Creates a predicate instance.
     *
     * @param data The NBT tag containing configuration for this predicate.
     * @return A new {@link Predicate<LivingEntity>} instance.
     */
    Predicate<LivingEntity> create(CompoundTag data);
}