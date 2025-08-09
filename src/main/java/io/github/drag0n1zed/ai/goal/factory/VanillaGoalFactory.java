package io.github.drag0n1zed.ai.goal.factory;

import io.github.drag0n1zed.api.ai.GoalDefinition;
import io.github.drag0n1zed.api.goal.IGoalRegistry;
import io.github.drag0n1zed.ai.goal.ConfigurableTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import static io.github.drag0n1zed.api.ai.validation.GoalValidators.*;

/**
 * A utility class to register all built-in vanilla goal definitions.
 * Each registration includes validation logic to ensure mob compatibility.
 */
public final class VanillaGoalFactory {

    private VanillaGoalFactory() {}

    public static void registerAll(IGoalRegistry registry) {

        // --- Universal Goals (apply to any Mob) ---
        registry.register(new GoalDefinition("FloatGoal", alwaysTrue(), (mob, data) -> new FloatGoal(mob)));
        registry.register(new GoalDefinition("LookAtPlayerGoal", alwaysTrue(), (mob, data) -> new LookAtPlayerGoal(mob, Player.class, 8.0F)));
        registry.register(new GoalDefinition("RandomLookAroundGoal", alwaysTrue(), (mob, data) -> new RandomLookAroundGoal(mob)));
        registry.register(new GoalDefinition("EatBlockGoal", alwaysTrue(), (mob, data) -> new EatBlockGoal(mob)));
        registry.register(new GoalDefinition("LeapAtTargetGoal", alwaysTrue(), (mob, data) -> new LeapAtTargetGoal(mob, 0.4F)));

        // --- PathfinderMob Goals ---
        registry.register(new GoalDefinition("MeleeAttackGoal", allOf(requiresClass(PathfinderMob.class), requiresAttribute(Attributes.ATTACK_DAMAGE.value())), (mob, data) -> new MeleeAttackGoal((PathfinderMob) mob, 1.0D, false)));
        registry.register(new GoalDefinition("override:configurable_target", requiresClass(PathfinderMob.class), io.github.drag0n1zed.ai.goal.ConfigurableTargetGoal::new));
        registry.register(new GoalDefinition("WaterAvoidingRandomStrollGoal", requiresClass(PathfinderMob.class), (mob, data) -> new WaterAvoidingRandomStrollGoal((PathfinderMob) mob, 1.0D)));
        registry.register(new GoalDefinition("PanicGoal", requiresClass(PathfinderMob.class), (mob, data) -> new PanicGoal((PathfinderMob) mob, 1.2D)));
        registry.register(new GoalDefinition("TemptGoal", requiresClass(PathfinderMob.class), (mob, data) -> new TemptGoal((PathfinderMob) mob, 1.1D, Ingredient.of(Items.WHEAT), false)));
        registry.register(new GoalDefinition(
                "FollowMobGoal",
                allOf(requiresClass(PathfinderMob.class), requiresNavigation(GroundPathNavigation.class)),
                (mob, data) -> new FollowMobGoal((PathfinderMob) mob, 1.0D, 10.0F, 2.0F)
        ));
        registry.register(new GoalDefinition("FleeSunGoal", requiresClass(PathfinderMob.class), (mob, data) -> new FleeSunGoal((PathfinderMob) mob, 1.0D)));
        registry.register(new GoalDefinition("BreathAirGoal", requiresClass(PathfinderMob.class), (mob, data) -> new BreathAirGoal((PathfinderMob) mob)));

        // --- Ground-Navigating PathfinderMob Goals ---
        registry.register(new GoalDefinition(
                "OpenDoorGoal",
                allOf(requiresClass(PathfinderMob.class), requiresNavigation(GroundPathNavigation.class)),
                (mob, data) -> new OpenDoorGoal((PathfinderMob) mob, false)
        ));

        // --- Specific Mob Subclass Goals ---
        registry.register(new GoalDefinition("SwellGoal", requiresClass(Creeper.class), (mob, data) -> new SwellGoal((Creeper) mob)));
        registry.register(new GoalDefinition("OfferFlowerGoal", requiresClass(IronGolem.class), (mob, data) -> new OfferFlowerGoal((IronGolem) mob)));
        registry.register(new GoalDefinition("FollowParentGoal", requiresClass(Animal.class), (mob, data) -> new FollowParentGoal((Animal) mob, 1.1D)));
        registry.register(new GoalDefinition("BreedGoal", requiresClass(Animal.class), (mob, data) -> new BreedGoal((Animal) mob, 1.0D)));
        registry.register(new GoalDefinition("RunAroundLikeCrazyGoal", requiresClass(AbstractHorse.class), (mob, data) -> new RunAroundLikeCrazyGoal((AbstractHorse) mob, 1.2D)));
        registry.register(new GoalDefinition("LlamaFollowCaravanGoal", requiresClass(Llama.class), (mob, data) -> new LlamaFollowCaravanGoal((Llama) mob, 2.1D)));
        registry.register(new GoalDefinition("BegGoal", requiresClass(Wolf.class), (mob, data) -> new BegGoal((Wolf) mob, 8.0F)));
        registry.register(new GoalDefinition("PandaSitGoal", requiresClass(Panda.class), (mob, data) -> ((Panda) mob).new PandaSitGoal()));
        registry.register(new GoalDefinition("CatRelaxOnOwnerGoal", requiresClass(Cat.class), (mob, data) -> new Cat.CatRelaxOnOwnerGoal((Cat) mob)));
        registry.register(new GoalDefinition("GhastLookGoal", requiresClass(Ghast.class), (mob, data) -> new Ghast.GhastLookGoal((Ghast) mob)));
        registry.register(new GoalDefinition("GhastShootFireballGoal", requiresClass(Ghast.class), (mob, data) -> new Ghast.GhastShootFireballGoal((Ghast) mob)));
        registry.register(new GoalDefinition("RandomFloatAroundGoal", requiresClass(Ghast.class), (mob, data) -> new Ghast.RandomFloatAroundGoal((Ghast) mob)));
        registry.register(new GoalDefinition("SlimeAttackGoal", requiresClass(Slime.class), (mob, data) -> new Slime.SlimeAttackGoal((Slime) mob)));
        registry.register(new GoalDefinition("SlimeFloatGoal", requiresClass(Slime.class), (mob, data) -> new Slime.SlimeFloatGoal((Slime) mob)));
        registry.register(new GoalDefinition("SlimeKeepOnJumpingGoal", requiresClass(Slime.class), (mob, data) -> new Slime.SlimeKeepOnJumpingGoal((Slime) mob)));
        registry.register(new GoalDefinition("SlimeRandomDirectionGoal", requiresClass(Slime.class), (mob, data) -> new Slime.SlimeRandomDirectionGoal((Slime) mob)));
        registry.register(new GoalDefinition("BlazeAttackGoal", requiresClass(Blaze.class), (mob, data) -> new Blaze.BlazeAttackGoal((Blaze) mob)));
        registry.register(new GoalDefinition("GuardianAttackGoal", requiresClass(Guardian.class), (mob, data) -> new Guardian.GuardianAttackGoal((Guardian) mob)));
        registry.register(new GoalDefinition("DrownedSwimUpGoal", requiresClass(Drowned.class), (mob, data) -> new Drowned.DrownedSwimUpGoal((Drowned) mob, 1.0D, 40)));

        // --- TamableAnimal Goals ---
        registry.register(new GoalDefinition("FollowOwnerGoal", requiresClass(TamableAnimal.class), (mob, data) -> new FollowOwnerGoal((TamableAnimal) mob, 1.0D, 10.0F, 2.0F)));
        registry.register(new GoalDefinition("SitWhenOrderedToGoal", requiresClass(TamableAnimal.class), (mob, data) -> new SitWhenOrderedToGoal((TamableAnimal) mob)));

        // --- TargetSelector Goals ---
        registry.register(new GoalDefinition("HurtByTargetGoal", requiresClass(PathfinderMob.class), (mob, data) -> new HurtByTargetGoal((PathfinderMob) mob)));
        registry.register(new GoalDefinition("NearestAttackableTargetGoal", alwaysTrue(), (mob, data) -> new NearestAttackableTargetGoal<>(mob, LivingEntity.class, true)));
        registry.register(new GoalDefinition("OwnerHurtByTargetGoal", requiresClass(TamableAnimal.class), (mob, data) -> new OwnerHurtByTargetGoal((TamableAnimal) mob)));
        registry.register(new GoalDefinition("OwnerHurtTargetGoal", requiresClass(TamableAnimal.class), (mob, data) -> new OwnerHurtTargetGoal((TamableAnimal) mob)));
        registry.register(new GoalDefinition("ResetUniversalAngerTargetGoal", allOf(requiresClass(PathfinderMob.class), requiresClass(NeutralMob.class)), (mob, data) -> createResetAngerGoal((PathfinderMob) mob)));
        registry.register(new GoalDefinition("ConfigurableTargetGoal", alwaysTrue(), (mob, data) -> new ConfigurableTargetGoal(mob, data)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Goal createResetAngerGoal(PathfinderMob mob) {
        return new ResetUniversalAngerTargetGoal(mob, false);
    }
}