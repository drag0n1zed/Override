package io.github.drag0n1zed.mixin;

import io.github.drag0n1zed.ai.GoalSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Injects at the end of NBT loading. If our custom AI flag is present,
     * this method now uses the GoalSerializer to reconstruct the custom AI,
     * overriding the default goals that were just registered.
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    protected void override_onReadAdditionalData(CompoundTag compound, CallbackInfo ci) {
        GoalSerializer.loadGoalsFromNBT((Mob) (Object) this);
    }
}