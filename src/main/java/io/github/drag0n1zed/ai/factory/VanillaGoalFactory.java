package io.github.drag0n1zed.ai.factory;

import io.github.drag0n1zed.ai.GoalSerializer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A utility class to register all built-in, publicly accessible vanilla goal factories.
 */
public final class VanillaGoalFactory {

    private VanillaGoalFactory() {}

    public static void registerAll(GoalSerializer registry) {

        // --- Universal Goals (apply to any Mob) ---
        registry.register("FloatGoal", (mob, data) -> new FloatGoal(mob));
        registry.register("LookAtPlayerGoal", (mob, data) -> new LookAtPlayerGoal(mob, Player.class, 8.0F));
        registry.register("RandomLookAroundGoal", (mob, data) -> new RandomLookAroundGoal(mob));
        registry.register("EatBlockGoal", (mob, data) -> new EatBlockGoal(mob));
        registry.register("LeapAtTargetGoal", (mob, data) -> new LeapAtTargetGoal(mob, 0.4F));
        registry.register("OpenDoorGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new OpenDoorGoal(p, false);
            return null;
        });

        // --- PathfinderMob Goals ---
        registry.register("MeleeAttackGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new MeleeAttackGoal(p, 1.0D, false);
            return null;
        });
        registry.register("WaterAvoidingRandomStrollGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new WaterAvoidingRandomStrollGoal(p, 1.0D);
            return null;
        });
        registry.register("PanicGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new PanicGoal(p, 1.2D);
            return null;
        });
        registry.register("TemptGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new TemptGoal(p, 1.1D, Ingredient.of(Items.WHEAT), false);
            return null;
        });
        registry.register("FollowMobGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new FollowMobGoal(p, 1.0D, 10.0F, 2.0F);
            return null;
        });
        registry.register("FleeSunGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new FleeSunGoal(p, 1.0D);
            return null;
        });
        registry.register("BreathAirGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new BreathAirGoal(p);
            return null;
        });

        // --- Specific Mob Subclass Goals ---
        registry.register("SwellGoal", (mob, data) -> {
            if (mob instanceof Creeper c) return new SwellGoal(c);
            return null;
        });
        registry.register("OfferFlowerGoal", (mob, data) -> {
            if (mob instanceof IronGolem g) return new OfferFlowerGoal(g);
            return null;
        });
        registry.register("FollowParentGoal", (mob, data) -> {
            if (mob instanceof Animal a) return new FollowParentGoal(a, 1.1D);
            return null;
        });
        registry.register("BreedGoal", (mob, data) -> {
            if (mob instanceof Animal a) return new BreedGoal(a, 1.0D);
            return null;
        });
        registry.register("RunAroundLikeCrazyGoal", (mob, data) -> {
            if (mob instanceof AbstractHorse h) return new RunAroundLikeCrazyGoal(h, 1.2D);
            return null;
        });
        registry.register("LlamaFollowCaravanGoal", (mob, data) -> {
            if (mob instanceof Llama l) return new LlamaFollowCaravanGoal(l, 2.1D);
            return null;
        });
        registry.register("BegGoal", (mob, data) -> {
            if (mob instanceof Wolf w) return new BegGoal(w, 8.0F);
            return null;
        });

        // --- TamableAnimal Goals ---
        registry.register("FollowOwnerGoal", (mob, data) -> {
            if (mob instanceof TamableAnimal t) return new FollowOwnerGoal(t, 1.0D, 10.0F, 2.0F);
            return null;
        });
        registry.register("SitWhenOrderedToGoal", (mob, data) -> {
            if (mob instanceof TamableAnimal t) return new SitWhenOrderedToGoal(t);
            return null;
        });

        // --- TargetSelector Goals ---
        registry.register("HurtByTargetGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p) return new HurtByTargetGoal(p);
            return null;
        });
        registry.register("NearestAttackableTargetGoal", (mob, data) -> {
            return new NearestAttackableTargetGoal<>(mob, LivingEntity.class, true);
        });
        registry.register("OwnerHurtByTargetGoal", (mob, data) -> {
            if (mob instanceof TamableAnimal t) return new OwnerHurtByTargetGoal(t);
            return null;
        });
        registry.register("OwnerHurtTargetGoal", (mob, data) -> {
            if (mob instanceof TamableAnimal t) return new OwnerHurtTargetGoal(t);
            return null;
        });
        registry.register("ResetUniversalAngerTargetGoal", (mob, data) -> {
            if (mob instanceof PathfinderMob p && mob instanceof NeutralMob) {
                return createResetAngerGoal(p);
            }
            return null;
        });
    }

    /**
     * Helper method to work around a Java generics limitation.
     * The ResetUniversalAngerTargetGoal constructor has a complex generic bound (<T extends Mob & NeutralMob>)
     * that the compiler cannot infer correctly from a lambda. This helper uses an unchecked raw type,
     * which is safe here because we have already performed the necessary `instanceof` checks.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Goal createResetAngerGoal(PathfinderMob mob) {
        return new ResetUniversalAngerTargetGoal(mob, false);
    }
}