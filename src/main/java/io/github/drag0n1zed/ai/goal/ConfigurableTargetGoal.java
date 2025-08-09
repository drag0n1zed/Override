package io.github.drag0n1zed.ai.goal;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.api.OverrideRegistries;
import io.github.drag0n1zed.api.ai.predicate.IPredicateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

/**
 * A configurable targeting goal that uses a dynamic set of predicates
 * to find a target. This goal is the foundation for user-defined targeting logic.
 */
public class ConfigurableTargetGoal extends TargetGoal {

    public static final String KEY_TARGET_CONDITIONS = "target_conditions";
    public static final String KEY_PREDICATE_ID = "id";

    private final Predicate<LivingEntity> combinedPredicate;
    protected LivingEntity target;

    public ConfigurableTargetGoal(Mob mob, CompoundTag parameters) {
        super(mob, false, false);
        this.combinedPredicate = buildCombinedPredicate(parameters);
        this.setFlags(java.util.EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        this.target = findTarget();
        return this.target != null;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    @Nullable
    protected LivingEntity findTarget() {
        List<LivingEntity> nearbyEntities = this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                this.mob.getBoundingBox().inflate(this.getFollowDistance(), 4.0D, this.getFollowDistance()),
                this.combinedPredicate
        );

        return this.mob.level().getNearestEntity(
                nearbyEntities,
                TargetingConditions.forCombat().range(this.getFollowDistance()),
                this.mob,
                this.mob.getX(), this.mob.getY(), this.mob.getZ()
        );
    }

    private Predicate<LivingEntity> buildCombinedPredicate(CompoundTag parameters) {
        IPredicateRegistry registry = OverrideRegistries.getPredicateRegistryProvider();
        if (registry == null) {
            OverrideMod.LOGGER.error("Predicate registry not available. ConfigurableTargetGoal will not target anything.");
            return entity -> false;
        }

        List<Predicate<LivingEntity>> predicates = new ArrayList<>();
        predicates.add(this.mob::isWithinMeleeAttackRange); // Basic vanilla check

        if (parameters.contains(KEY_TARGET_CONDITIONS, Tag.TAG_LIST)) {
            ListTag conditions = parameters.getList(KEY_TARGET_CONDITIONS, Tag.TAG_COMPOUND);
            for (Tag tag : conditions) {
                if (tag instanceof CompoundTag conditionTag) {
                    String id = conditionTag.getString(KEY_PREDICATE_ID);
                    registry.getDefinition(id).ifPresentOrElse(
                            def -> predicates.add(def.factory().create(conditionTag)),
                            () -> OverrideMod.LOGGER.warn("Unknown predicate identifier '{}' in ConfigurableTargetGoal. Skipping.", id)
                    );
                }
            }
        }

        return predicates.stream().reduce(Predicate::and).orElse(entity -> true);
    }
}