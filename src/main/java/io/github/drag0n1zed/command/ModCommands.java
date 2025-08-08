package io.github.drag0n1zed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.drag0n1zed.ai.GoalType;
import io.github.drag0n1zed.block.SurgicalStationBlockEntity;
import io.github.drag0n1zed.registration.ModEffects;
import io.github.drag0n1zed.ai.AIGoalData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("override")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stun").executes(ModCommands::executeStun))
                .then(Commands.literal("clear").executes(ModCommands::executeClear))
                .then(Commands.literal("debug_ray").executes(ModCommands::executeDebugRay))
                .then(Commands.literal("inspect").executes(ModCommands::inspectEntity))
                .then(Commands.literal("capture").executes(ModCommands::captureEntity))
                .then(Commands.literal("release").executes(ModCommands::releaseEntity))
                .then(Commands.literal("test_wipe_ai").executes(ModCommands::executeWipeAI))
                .then(Commands.literal("test_apply_zombie_ai").executes(ModCommands::executeApplyZombieAI))
        );
    }

    private static int executeApplyZombieAI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        HitResult hitResult = source.getPlayerOrException().pick(20, 0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("You are not looking at a block."));
            return 0;
        }

        BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity blockEntity = source.getLevel().getBlockEntity(blockPos);

        if (!(blockEntity instanceof SurgicalStationBlockEntity station)) {
            source.sendFailure(Component.literal("You are not looking at a Surgical Station."));
            return 0;
        }

        if (!station.hasCapturedEntity()) {
            source.sendFailure(Component.literal("This Surgical Station is empty."));
            return 0;
        }

        // Define the AI goals of a Zombie, omitting ones with complex constructors for now.
        List<AIGoalData> zombieGoals = List.of(
                new AIGoalData(GoalType.GOAL, 0, "FloatGoal", false),
                new AIGoalData(GoalType.GOAL, 2, "MeleeAttackGoal", false),
                new AIGoalData(GoalType.GOAL, 5, "WaterAvoidingRandomStrollGoal", false),
                new AIGoalData(GoalType.GOAL, 6, "LookAtPlayerGoal", false),
                new AIGoalData(GoalType.GOAL, 7, "RandomLookAroundGoal", false),
                new AIGoalData(GoalType.TARGET, 1, "HurtByTargetGoal", false),
                new AIGoalData(GoalType.TARGET, 2, "NearestAttackableTargetGoal", false)
        );

        boolean success = station.applyAIModification(zombieGoals);
        if (success) {
            source.sendSuccess(() -> Component.literal("Successfully applied Zombie AI to captured entity."), true);
        } else {
            source.sendFailure(Component.literal("Failed to apply AI modification."));
        }
        return success ? 1 : 0;
    }

    private static int executeWipeAI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        HitResult hitResult = source.getPlayerOrException().pick(20, 0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("You are not looking at a block."));
            return 0;
        }

        BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity blockEntity = source.getLevel().getBlockEntity(blockPos);

        if (!(blockEntity instanceof SurgicalStationBlockEntity station)) {
            source.sendFailure(Component.literal("You are not looking at a Surgical Station."));
            return 0;
        }

        if (!station.hasCapturedEntity()) {
            source.sendFailure(Component.literal("This Surgical Station is empty."));
            return 0;
        }

        // To wipe the AI, we apply an empty list of goals.
        boolean success = station.applyAIModification(Collections.emptyList());

        if (success) {
            source.sendSuccess(() -> Component.literal("Successfully applied AI wipe to captured entity."), true);
        } else {
            source.sendFailure(Component.literal("Failed to apply AI modification."));
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
        HitResult hitResult = source.getPlayerOrException().pick(20, 0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("You are not looking at a block."));
            return 0;
        }

        BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity blockEntity = source.getLevel().getBlockEntity(blockPos);

        if (!(blockEntity instanceof SurgicalStationBlockEntity station)) {
            source.sendFailure(Component.literal("You are not looking at a Surgical Station."));
            return 0;
        }

        if (!station.hasCapturedEntity()) {
            source.sendFailure(Component.literal("This Surgical Station is empty."));
            return 0;
        }

        CompoundTag mobNBT = station.getCapturedEntityNBT();
        Optional<Entity> entityOptional = EntityType.create(mobNBT, source.getLevel());

        if (entityOptional.isEmpty()) {
            source.sendFailure(Component.literal("Failed to recreate entity from NBT data. Data may be corrupted."));
            return 0;
        }

        Entity releasedEntity = entityOptional.get();
        releasedEntity.setPos(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5);
        source.getLevel().addFreshEntity(releasedEntity);
        station.releaseEntity();

        source.sendSuccess(() -> Component.literal("Released " + releasedEntity.getName().getString() + " from station."), true);
        return 1;
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

        Set<WrappedGoal> goals = mob.goalSelector.getAvailableGoals();
        Set<WrappedGoal> targeters = mob.targetSelector.getAvailableGoals();

        source.sendSuccess(() -> Component.literal("--- AI GOALS for " + mob.getName().getString() + " ---").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Goal Selector:").withStyle(ChatFormatting.AQUA), false);
        goals.stream().map(ModCommands::formatGoal).forEach(c -> source.sendSuccess(() -> c, false));

        source.sendSuccess(() -> Component.literal("Target Selector:").withStyle(ChatFormatting.RED), false);
        targeters.stream().map(ModCommands::formatGoal).forEach(c -> source.sendSuccess(() -> c, false));

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
}