package io.github.drag0n1zed.event;

import io.github.drag0n1zed.block.menu.SurgicalStationScreen;
import io.github.drag0n1zed.registration.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static io.github.drag0n1zed.OverrideMod.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client-side setup logic
    }

    @SubscribeEvent
    public static void registerGuiScreens(final RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SURGICAL_STATION_MENU.get(), SurgicalStationScreen::new);
    }
}
