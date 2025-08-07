package io.github.drag0n1zed.mixin;

import io.github.drag0n1zed.api.StunnedEntityAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements StunnedEntityAccessor {

    // Define the data tracker key. "Unique" makes sure it doesn't conflict.
    @Unique
    private static final EntityDataAccessor<Boolean> override_IS_STUNNED =
            SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.BOOLEAN);

    // Constructor needed for Mixins
    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Inject into the method that defines all data trackers for an entity.
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void override_defineSynchedData(SynchedEntityData.Builder pBuilder, CallbackInfo ci) {
        pBuilder.define(override_IS_STUNNED, false);
    }

    // Make sure our data is saved and loaded with the entity.
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    protected void override_addAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putBoolean("OverrideIsStunned", this.override_isStunned());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    protected void override_readAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        this.override_setStunned(pCompound.getBoolean("OverrideIsStunned"));
    }

    // Implement our accessor interface methods.
    @Unique
    @Override
    public boolean override_isStunned() {
        return this.entityData.get(override_IS_STUNNED);
    }

    @Unique
    @Override
    public void override_setStunned(boolean isStunned) {
        this.entityData.set(override_IS_STUNNED, isStunned);
    }
}
