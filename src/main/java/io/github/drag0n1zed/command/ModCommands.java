package io.github.drag0n1zed.command;

import io.github.drag0n1zed.core.ModEffects;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("override")
                .then(Commands.literal("stun")
                        .executes(ModCommands::executeStun)
                )
                .then(Commands.literal("clear")
                        .executes(ModCommands::executeClear)
                )
                .then(Commands.literal("debug_ray")
                        .executes(ModCommands::executeDebugRay)
                )
        );
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

    private static int executeStun(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return applyToLookedAtEntity(context, true);
    }

    private static int executeClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return applyToLookedAtEntity(context, false);
    }

    private static int applyToLookedAtEntity(CommandContext<CommandSourceStack> context, boolean apply) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Entity commandSender = source.getEntity();

        if (commandSender == null) {
            source.sendFailure(Component.literal("Command must be run by an entity."));
            return 0;
        }

        // --- Start of targeting logic ---
        double reachDistance = 20.0;
        LivingEntity target = null;
        double closestFoundDistanceSqr = Double.MAX_VALUE;

        Vec3 eyePos = commandSender.getEyePosition();
        Vec3 lookVec = commandSender.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(reachDistance));

        AABB searchBox = commandSender.getBoundingBox().expandTowards(lookVec.scale(reachDistance)).inflate(1.0D, 1.0D, 1.0D);

        for (Entity entity : commandSender.level().getEntities(commandSender, searchBox, (e) -> e instanceof LivingEntity && e.isPickable())) {
            AABB entityHitbox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> optionalHitPos = entityHitbox.clip(eyePos, reachVec);

            if (optionalHitPos.isPresent()) {
                double distSqr = eyePos.distanceToSqr(optionalHitPos.get());
                if (distSqr < closestFoundDistanceSqr) {
                    closestFoundDistanceSqr = distSqr;
                    target = (LivingEntity) entity;
                }
            }
        }
        // --- End of targeting logic ---

        if (target != null) {
            final LivingEntity finalTarget = target;

            if (apply) {
                // The action itself can use the original variable
                finalTarget.addEffect(new MobEffectInstance(ModEffects.STUNNED_EFFECT, 600));
                // The lambda for the message must use the final variable
                source.sendSuccess(() -> Component.literal("Applied Stunned to " + finalTarget.getName().getString()), true);
            } else {
                finalTarget.removeEffect(ModEffects.STUNNED_EFFECT);
                source.sendSuccess(() -> Component.literal("Removed Stunned from " + finalTarget.getName().getString()), true);
            }
            return 1;
        }

        source.sendFailure(Component.literal("You are not looking at a valid entity."));
        return 0;
    }
}