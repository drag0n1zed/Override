package io.github.drag0n1zed.event;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.command.ModCommands;
import io.github.drag0n1zed.core.ModEffects;
import io.github.drag0n1zed.api.StunnedEntityAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = OverrideMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        // Check if the effect being added is our Stunned effect
        if (event.getEffectInstance().getEffect().is(ModEffects.STUNNED_EFFECT.getKey())) {
            LivingEntity entity = event.getEntity();
            // If the entity can be shaken, turn on the shake
            if (entity instanceof StunnedEntityAccessor accessor) {
                accessor.override_setStunned(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        // Check if the effect that expired is our Stunned effect
        assert event.getEffectInstance() != null;
        if (event.getEffectInstance().getEffect().is(ModEffects.STUNNED_EFFECT.getKey())) {
            LivingEntity entity = event.getEntity();
            // If the entity can be shaken, turn off the shake
            if (entity instanceof StunnedEntityAccessor accessor) {
                accessor.override_setStunned(false);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        // Check if the effect being removed is our Stunned effect
        assert event.getEffectInstance() != null;
        if (event.getEffectInstance().getEffect().is(ModEffects.STUNNED_EFFECT.getKey())) {
            LivingEntity entity = event.getEntity();
            // If the entity can be shaken, turn off the shake
            if (entity instanceof StunnedEntityAccessor accessor) {
                accessor.override_setStunned(false);
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}