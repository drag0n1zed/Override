package io.github.drag0n1zed.mixin;

import io.github.drag0n1zed.accessor.StunnedEntityAccessor;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This Mixin hooks into the isShaking method of the base LivingEntityRenderer.
 * It follows the vanilla pattern seen in ZombieVillagerRenderer to make any
 * entity shake if it has our custom "Stunned" state.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    /**
     * Injects at the HEAD of the isShaking method.
     * We make it cancellable so we can override its return value.
     * @param entity The entity being rendered.
     * @param cir The CallbackInfoReturnable, which allows us to set the return value.
     */
    @Inject(
            method = "isShaking(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void override_isShaking(T entity, CallbackInfoReturnable<Boolean> cir) {
        // Cast the entity to our accessor to check the custom data flag.
        StunnedEntityAccessor accessor = (StunnedEntityAccessor) entity;

        // If our custom "isStunned" flag is true...
        if (accessor.override_isStunned()) {
            // ...force the isShaking method to immediately return true.
            cir.setReturnValue(true);
        }

        // If our flag is false, we do nothing. The original method code will run,
        // which checks for the frozen effect (entity.isFullyFrozen()).
    }
}