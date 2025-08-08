package io.github.drag0n1zed;

import com.mojang.logging.LogUtils;
import io.github.drag0n1zed.ai.GoalSerializer;
import io.github.drag0n1zed.registration.ModBlocks;
import io.github.drag0n1zed.registration.ModBlockEntities;
import io.github.drag0n1zed.registration.ModEffects;
import io.github.drag0n1zed.registration.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

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
        GoalSerializer.initialize();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup logic
    }
}
