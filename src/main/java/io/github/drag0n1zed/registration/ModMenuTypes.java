package io.github.drag0n1zed.registration;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.block.menu.SurgicalStationMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, OverrideMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SurgicalStationMenu>> SURGICAL_STATION_MENU =
            MENUS.register("surgical_station_menu",
                    () -> IMenuTypeExtension.create(SurgicalStationMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
