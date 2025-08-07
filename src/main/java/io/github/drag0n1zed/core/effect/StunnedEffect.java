package io.github.drag0n1zed.core.effect;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.api.StunnedEntityAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class StunnedEffect extends MobEffect {

    public StunnedEffect() {
        super(MobEffectCategory.HARMFUL, 0xcba6f7);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(OverrideMod.MODID, "stunned_effect"),
                -0.90D, // 90% reduction in speed. Feels almost like a complete stop.
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // Apply your effect logic here.
        // If this returns false when shouldApplyEffectTickThisTick returns true, the effect will immediately be removed
        return true;
    }

    // Whether the effect should apply this tick. Used e.g. by the Regeneration effect that only applies
    // once every x ticks, depending on the tick count and amplifier.
    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        // return tickCount % 2 == 0;
        return false;
    }

    // Utility method that is called when the effect is first added to the entity.
    // This does not get called again until all instances of this effect have been removed from the entity.
    @Override
    public void onEffectAdded(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof StunnedEntityAccessor accessor) {
            accessor.override_setStunned(true);
        }
        super.onEffectAdded(entity, amplifier);
    }



    // Utility method that is called when the effect is added to the entity.
    // This gets called every time this effect is added to the entity.
    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
    }
}
