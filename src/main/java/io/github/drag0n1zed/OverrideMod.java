package io.github.drag0n1zed;

import io.github.drag0n1zed.block.ModBlocks;
import io.github.drag0n1zed.block.entity.ModBlockEntities;
import io.github.drag0n1zed.command.ModCommands;
import io.github.drag0n1zed.core.ModEffects;
import io.github.drag0n1zed.screen.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(OverrideMod.MODID)
public class OverrideMod {
    public static final String MODID = "override";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OverrideMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Override Mod");

        ModEffects.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
    }
}
