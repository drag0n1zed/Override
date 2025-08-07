package io.github.drag0n1zed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.drag0n1zed.command.util.CommandHelper;
import io.github.drag0n1zed.core.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("override")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stun")
                        .executes(ModCommands::executeStun)
                )
                .then(Commands.literal("clear")
                        .executes(ModCommands::executeClear)
                )
                .then(Commands.literal("debug_ray")
                        .executes(ModCommands::executeDebugRay)
                )
                .then(Commands.literal("inspect")
                        .executes(ModCommands::inspectLookingAtEntity))
        );
    }

    private static int inspectLookingAtEntity(CommandContext<CommandSourceStack> context) {
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
        if (goals.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  (None)").withStyle(ChatFormatting.GRAY), false);
        } else {
            goals.stream().map(ModCommands::formatGoal).forEach(c -> source.sendSuccess(() -> c, false));
        }

        source.sendSuccess(() -> Component.literal("Target Selector:").withStyle(ChatFormatting.RED), false);
        if (targeters.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  (None)").withStyle(ChatFormatting.GRAY), false);
        } else {
            targeters.stream().map(ModCommands::formatGoal).forEach(c -> source.sendSuccess(() -> c, false));
        }

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
            for (int i = 0; i < 40; i++) { // Draw for 20 blocks (40 steps of 0.5 blocks)
                Vec3 particlePos = eyePos.add(lookVec.scale(i * 0.5));
                serverLevel.sendParticles(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            source.sendSuccess(() -> Component.literal("Drew debug ray!"), false);
            return 1;
        }
        return 0;
    }
}