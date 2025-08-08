package io.github.drag0n1zed.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CommandHelper {

    /**
     * Finds the LivingEntity a command sender is looking at, up to a given distance.
     * @param source The command source, expected to be a player.
     * @param maxDistance The maximum distance to check.
     * @return The targeted LivingEntity, or null if none is found.
     */
    @Nullable
    public static LivingEntity getTargetedEntity(CommandSourceStack source, double maxDistance) {
        Entity commandSender = source.getEntity();
        if (commandSender == null) {
            return null;
        }

        LivingEntity target = null;
        double closestFoundDistanceSqr = Double.MAX_VALUE;

        Vec3 eyePos = commandSender.getEyePosition();
        Vec3 lookVec = commandSender.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(maxDistance));

        AABB searchBox = commandSender.getBoundingBox().expandTowards(lookVec.scale(maxDistance)).inflate(1.0D, 1.0D, 1.0D);

        for (Entity entity : commandSender.level().getEntities(commandSender, searchBox, e -> e instanceof LivingEntity && e.isPickable())) {
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
        return target;
    }
}