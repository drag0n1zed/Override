package io.github.drag0n1zed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.drag0n1zed.ai.AIGoalData;
import io.github.drag0n1zed.ai.GoalType;
import io.github.drag0n1zed.api.OverrideRegistries;
import io.github.drag0n1zed.api.ai.GoalDefinition;
import io.github.drag0n1zed.api.goal.IGoalRegistry;
import io.github.drag0n1zed.block.SurgicalStationBlockEntity;
import io.github.drag0n1zed.registration.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ModCommands {

    // Removed TEST_SUITE_MOBS constant - now generated dynamically in executeTestSuite


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("override")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("capture").executes(ModCommands::captureEntity))
                .then(Commands.literal("release").executes(ModCommands::releaseEntity))
                .then(Commands.literal("inspect").executes(ModCommands::inspectEntity))
                .then(Commands.literal("query_compatible").executes(ModCommands::queryCompatibleGoals))
                .then(Commands.literal("test_suite").executes(ModCommands::executeTestSuite))
                .then(Commands.literal("test_all_compatible").executes(ModCommands::executeSingleMobStressTest))
                .then(Commands.literal("test")
                        .then(Commands.literal("wipe_ai").executes(ModCommands::executeWipeAI))
                        .then(Commands.literal("apply_cowbie_ai").executes(ctx -> executeAITest(ctx, getCowbieTestGoals(), "Cow-bie")))
                        .then(Commands.literal("apply_creeper_pig_ai").executes(ctx -> executeAITest(ctx, getCreeperTestGoals(), "Creeper-in-a-Box")))
                        .then(Commands.literal("apply_ghast_ai").executes(ctx -> executeAITest(ctx, getGhastTestGoals(), "Ghast Unleashed")))
                        .then(Commands.literal("apply_angry_creeper_ai").executes(ctx -> executeAITest(ctx, getAngryCreeperTestGoals(), "Angry-Creeper")))
                        .then(Commands.literal("apply_loyal_wolf_ai").executes(ctx -> executeAITest(ctx, getLoyalWolfTestGoals(), "Loyal-Wolf")))
                )
                .then(Commands.literal("stun").executes(ModCommands::executeStun))
                .then(Commands.literal("clear").executes(ModCommands::executeClear))
                .then(Commands.literal("debug_ray").executes(ModCommands::executeDebugRay))
        );
    }

    private static int executeTestSuite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos playerPos = BlockPos.containing(source.getPosition());
        SurgicalStationBlockEntity station = findNearestVacantStation(source.getLevel(), playerPos, 16);

        if (station == null) {
            source.sendFailure(Component.literal("Test Suite requires a vacant Surgical Station within 16 blocks."));
            return 0;
        }

        // Generate the mob list dynamically
        final List<EntityType<? extends Mob>> testSuiteMobs =
                BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(type -> {
                            if (type == EntityType.PLAYER) return false; // Explicitly skip player
                            try {
                                // Attempt to create a temporary entity to check its type.
                                Entity tempEntity = type.create(source.getLevel());
                                return tempEntity instanceof Mob;
                            } catch (Exception e) {
                                // If any exception occurs during creation (like the NPE),
                                // assume it's not a standard, testable mob and skip it.
                                return false;
                            }
                        })
                        .map(type -> (EntityType<? extends Mob>) type)
                        .collect(Collectors.toList());

        source.sendSuccess(() -> Component.literal("===== STARTING AUTOMATED TEST SUITE (" + testSuiteMobs.size() + " mob types) =====").withStyle(ChatFormatting.GOLD), true);

        for (EntityType<? extends Mob> mobType : testSuiteMobs) {
            source.sendSuccess(() -> Component.literal("--- Testing " + EntityType.getKey(mobType).getPath().toUpperCase() + " ---").withStyle(ChatFormatting.YELLOW), true);
            Mob mobToTest = mobType.create(source.getLevel());

            if (mobToTest == null) {
                source.sendFailure(Component.literal("Failed to create mob of type: " + EntityType.getKey(mobType)));
                continue;
            }

            station.captureEntity(mobToTest);
            runCompatibilityStressTestOnStation(source, station);
        }

        source.sendSuccess(() -> Component.literal("===== TEST SUITE COMPLETE =====").withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    private static int executeSingleMobStressTest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        SurgicalStationBlockEntity station = getTargetedStation(source);
        if (station == null) return 0;

        runCompatibilityStressTestOnStation(source, station);
        return 1;
    }

    private static void runCompatibilityStressTestOnStation(CommandSourceStack source, SurgicalStationBlockEntity station) {
        IGoalRegistry registry = OverrideRegistries.getGoalRegistryProvider();
        if (registry == null) {
            source.sendFailure(Component.literal("Override Registry not available."));
            return;
        }

        Optional<Entity> entityOpt = EntityType.create(station.getCapturedEntityNBT(), source.getLevel());
        if (entityOpt.isEmpty() || !(entityOpt.get() instanceof Mob mob)) {
            source.sendFailure(Component.literal("Could not create mob from station NBT."));
            station.releaseEntity();
            return;
        }

        source.sendSuccess(() -> Component.literal("Starting compatibility stress test for ")
                .append(mob.getName().copy().withStyle(ChatFormatting.AQUA))
                .append("..."), false);

        List<GoalDefinition> compatibleGoals = registry.getCompatibleDefinitions(mob);
        int successCount = 0;
        for (GoalDefinition definition : compatibleGoals) {
            Goal dummyGoal = definition.factory().create(mob, new CompoundTag());
            if (dummyGoal == null) {
                source.sendFailure(Component.literal(" - Test failed for '").append(definition.identifier()).append("': Factory returned null."));
                continue;
            }
            GoalType goalType = (dummyGoal instanceof TargetGoal) ? GoalType.TARGET : GoalType.GOAL;

            List<AIGoalData> testProfile = List.of(
                    new AIGoalData(GoalType.GOAL, 0, "FloatGoal", false),
                    new AIGoalData(goalType, 1, definition.identifier(), false)
            );

            boolean success = station.applyAIModification(testProfile);
            if (success) successCount++;
        }

        final int finalSuccessCount = successCount;
        if (finalSuccessCount == compatibleGoals.size()) {
            source.sendSuccess(() -> Component.literal(String.format("Result: PASSED (%d/%d goals)", finalSuccessCount, compatibleGoals.size())).withStyle(ChatFormatting.GREEN), false);
        } else {
            source.sendSuccess(() -> Component.literal(String.format("Result: FAILED (%d/%d goals passed)", finalSuccessCount, compatibleGoals.size())).withStyle(ChatFormatting.RED), false);
        }

        releaseEntityFromStation(source, station);
    }

    private static int queryCompatibleGoals(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        SurgicalStationBlockEntity station = getTargetedStation(source);
        if (station == null) return 0;

        IGoalRegistry registry = OverrideRegistries.getGoalRegistryProvider();;
        if (registry == null) {
            source.sendFailure(Component.literal("Override Registry not available."));
            return 0;
        }

        Optional<Entity> entityOpt = EntityType.create(station.getCapturedEntityNBT(), source.getLevel());
        if (entityOpt.isEmpty() || !(entityOpt.get() instanceof Mob mob)) {
            source.sendFailure(Component.literal("Could not create mob from station NBT."));
            return 0;
        }

        List<GoalDefinition> compatibleGoals = registry.getCompatibleDefinitions(mob);

        source.sendSuccess(() -> Component.literal("Compatible goals for ")
                .append(mob.getName().copy().withStyle(ChatFormatting.AQUA))
                .append(":"), false);

        String goalList = compatibleGoals.stream()
                .map(GoalDefinition::identifier)
                .sorted()
                .collect(Collectors.joining(", "));

        source.sendSuccess(() -> Component.literal(goalList).withStyle(ChatFormatting.GREEN), false);

        return compatibleGoals.size();
    }

    private static int executeAITest(CommandContext<CommandSourceStack> context, List<AIGoalData> goals, String testName) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        SurgicalStationBlockEntity station = getTargetedStation(source);

        if (station == null) {
            return 0;
        }

        boolean success = station.applyAIModification(goals);
        if (success) {
            source.sendSuccess(() -> Component.literal("Test '" + testName + "' applied successfully.").withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendFailure(Component.literal("Failed to apply AI for test '" + testName + "'. See logs for details."));
        }
        return success ? 1 : 0;
    }

    //<editor-fold desc="Test Goal Definitions">
    private static List<AIGoalData> getCowbieTestGoals() {
        return List.of(
                new AIGoalData(GoalType.GOAL, 0, "FloatGoal", false),
                new AIGoalData(GoalType.GOAL, 2, "MeleeAttackGoal", false),
                new AIGoalData(GoalType.GOAL, 5, "WaterAvoidingRandomStrollGoal", false),
                new AIGoalData(GoalType.GOAL, 6, "LookAtPlayerGoal", false),
                new AIGoalData(GoalType.GOAL, 7, "RandomLookAroundGoal", false),
                new AIGoalData(GoalType.TARGET, 1, "HurtByTargetGoal", false),
                new AIGoalData(GoalType.TARGET, 2, "NearestAttackableTargetGoal", false)
        );
    }

    private static List<AIGoalData> getCreeperTestGoals() {
        return List.of(
                new AIGoalData(GoalType.GOAL, 0, "FloatGoal", false),
                new AIGoalData(GoalType.GOAL, 1, "SwellGoal", false),
                new AIGoalData(GoalType.GOAL, 4, "MeleeAttackGoal", false),
                new AIGoalData(GoalType.TARGET, 1, "HurtByTargetGoal", false)
        );
    }

    private static List<AIGoalData> getGhastTestGoals() {
        return List.of(
                new AIGoalData(GoalType.GOAL, 5, "RandomFloatAroundGoal", false),
                new AIGoalData(GoalType.GOAL, 7, "GhastLookGoal", false),
                new AIGoalData(GoalType.GOAL, 7, "GhastShootFireballGoal", false),
                new AIGoalData(GoalType.TARGET, 1, "NearestAttackableTargetGoal", false)
        );
    }

    private static List<AIGoalData> getAngryCreeperTestGoals() {
        return List.of(
                new AIGoalData(GoalType.GOAL, 0, "FloatGoal", false),
                new AIGoalData(GoalType.GOAL, 2, "MeleeAttackGoal", false),
                new AIGoalData(GoalType.TARGET, 1, "HurtByTargetGoal", false),
                new AIGoalData(GoalType.TARGET, 2, "ResetUniversalAngerTargetGoal", false)
        );
    }

    private static List<AIGoalData> getLoyalWolfTestGoals() {
        return List.of(
                new AIGoalData(GoalType.GOAL, 0, "FloatGoal", false),
                new AIGoalData(GoalType.GOAL, 1, "SitWhenOrderedToGoal", false),
                new AIGoalData(GoalType.GOAL, 2, "FollowOwnerGoal", false),
                new AIGoalData(GoalType.GOAL, 5, "WaterAvoidingRandomStrollGoal", false),
                new AIGoalData(GoalType.TARGET, 1, "OwnerHurtByTargetGoal", false),
                new AIGoalData(GoalType.TARGET, 2, "OwnerHurtTargetGoal", false)
        );
    }
    //</editor-fold>

    private static int executeWipeAI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        SurgicalStationBlockEntity station = getTargetedStation(source);

        if (station == null) {
            return 0;
        }

        boolean success = station.applyAIModification(Collections.emptyList());
        if (success) {
            source.sendSuccess(() -> Component.literal("Successfully wiped AI from captured entity."), true);
        } else {
            source.sendFailure(Component.literal("Failed to apply AI wipe."));
        }
        return success ? 1 : 0;
    }

    private static int captureEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        LivingEntity target = CommandHelper.getTargetedEntity(source, 20.0);

        if (!(target instanceof Mob mob)) {
            source.sendFailure(Component.literal("You must be looking at a valid Mob to capture."));
            return 0;
        }

        BlockPos playerPos = BlockPos.containing(source.getPosition());
        SurgicalStationBlockEntity station = findNearestVacantStation(source.getLevel(), playerPos, 16);

        if (station == null) {
            source.sendFailure(Component.literal("No vacant Surgical Station found within 16 blocks."));
            return 0;
        }

        station.captureEntity(mob);
        mob.discard();

        source.sendSuccess(() -> Component.literal("Captured " + mob.getName().getString() + " into station at " + station.getBlockPos().toShortString()), true);
        return 1;
    }

    private static int releaseEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        SurgicalStationBlockEntity station = getTargetedStation(source);
        if (station != null) {
            releaseEntityFromStation(source, station);
        }
        return 1;
    }

    private static void releaseEntityFromStation(CommandSourceStack source, SurgicalStationBlockEntity station) {
        CompoundTag mobNBT = station.getCapturedEntityNBT();
        if (mobNBT == null || mobNBT.isEmpty()) {
            return;
        }

        Optional<Entity> entityOptional = EntityType.create(mobNBT, source.getLevel());

        if (entityOptional.isEmpty()) {
            source.sendFailure(Component.literal("Failed to recreate entity from NBT data. Data may be corrupted."));
            station.releaseEntity();
            return;
        }

        Entity releasedEntity = entityOptional.get();
        releasedEntity.setPos(station.getBlockPos().getX() + 0.5, station.getBlockPos().getY() + 1.0, station.getBlockPos().getZ() + 0.5);
        source.getLevel().addFreshEntity(releasedEntity);
        station.releaseEntity();

        source.sendSuccess(() -> Component.literal("Released " + releasedEntity.getName().getString() + " from station.").withStyle(ChatFormatting.GRAY), false);
    }

    private static SurgicalStationBlockEntity findNearestVacantStation(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.withinManhattan(center, radius, radius, radius)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SurgicalStationBlockEntity station && !station.hasCapturedEntity()) {
                return station;
            }
        }
        return null;
    }

    private static SurgicalStationBlockEntity getTargetedStation(CommandSourceStack source) throws CommandSyntaxException {
        HitResult hitResult = source.getPlayerOrException().pick(20, 0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("You are not looking at a block."));
            return null;
        }

        BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity blockEntity = source.getLevel().getBlockEntity(blockPos);

        if (!(blockEntity instanceof SurgicalStationBlockEntity station)) {
            source.sendFailure(Component.literal("You are not looking at a Surgical Station."));
            return null;
        }

        if (!station.hasCapturedEntity()) {
            source.sendFailure(Component.literal("This Surgical Station is empty."));
            return null;
        }
        return station;
    }

    //<editor-fold desc="Utility & Debug Commands">
    private static int inspectEntity(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        LivingEntity target = CommandHelper.getTargetedEntity(source, 20.0);

        if (target == null) {
            source.sendFailure(Component.literal("You are not looking at a valid entity."));
            return 0;
        }

        if (!(target instanceof Mob mob)) {
            source.sendFailure(Component.literal("Target entity is not a Mob and has no AI goals."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("--- AI GOALS for " + mob.getName().getString() + " ---").withStyle(ChatFormatting.GOLD), false);
        BiConsumer<String, Set<WrappedGoal>> printGoals = (title, goals) -> {
            source.sendSuccess(() -> Component.literal(title).withStyle(ChatFormatting.AQUA), false);
            if (goals.isEmpty()) {
                source.sendSuccess(() -> Component.literal("  (None)").withStyle(ChatFormatting.GRAY), false);
            } else {
                goals.stream().map(ModCommands::formatGoal).forEach(c -> source.sendSuccess(() -> c, false));
            }
        };

        printGoals.accept("Goal Selector:", mob.goalSelector.getAvailableGoals());
        printGoals.accept("Target Selector:", mob.targetSelector.getAvailableGoals());

        return 1;
    }

    private static Component formatGoal(WrappedGoal wrappedGoal) {
        int priority = wrappedGoal.getPriority();
        Goal goal = wrappedGoal.getGoal();
        String simpleName = goal.getClass().getSimpleName();
        return Component.literal(String.format("  [%d] %s", priority, simpleName));
    }

    private static int executeStun(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return applyToLookedAtEntity(context, true);
    }

    private static int executeClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return applyToLookedAtEntity(context, false);
    }

    private static int applyToLookedAtEntity(CommandContext<CommandSourceStack> context, boolean apply) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        LivingEntity target = CommandHelper.getTargetedEntity(source, 20.0);

        if (target != null) {
            if (apply) {
                target.addEffect(new MobEffectInstance(ModEffects.STUNNED_EFFECT, 600));
                source.sendSuccess(() -> Component.literal("Applied Stunned to " + target.getName().getString()), true);
            } else {
                target.removeEffect(ModEffects.STUNNED_EFFECT);
                source.sendSuccess(() -> Component.literal("Removed Stunned from " + target.getName().getString()), true);
            }
            return 1;
        }
        source.sendFailure(Component.literal("You are not looking at a valid entity."));
        return 0;
    }

    private static int executeDebugRay(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Entity commandSender = source.getEntity();
        if (commandSender != null && commandSender.level() instanceof ServerLevel serverLevel) {
            Vec3 eyePos = commandSender.getEyePosition();
            Vec3 lookVec = commandSender.getLookAngle();
            for (int i = 0; i < 40; i++) {
                Vec3 particlePos = eyePos.add(lookVec.scale(i * 0.5));
                serverLevel.sendParticles(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            source.sendSuccess(() -> Component.literal("Drew debug ray!"), false);
            return 1;
        }
        return 0;
    }
    //</editor-fold>
}