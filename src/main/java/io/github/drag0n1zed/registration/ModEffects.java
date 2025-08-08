package io.github.drag0n1zed.registration;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.effect.StunnedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects
{
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, OverrideMod.MODID);

    public static final DeferredHolder<MobEffect, StunnedEffect> STUNNED_EFFECT =
            MOB_EFFECTS.register("stunned_effect", StunnedEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
