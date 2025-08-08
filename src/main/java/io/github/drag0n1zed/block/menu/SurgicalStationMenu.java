package io.github.drag0n1zed.block.menu;

import io.github.drag0n1zed.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SurgicalStationMenu extends AbstractContainerMenu {

    public SurgicalStationMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.SURGICAL_STATION_MENU.get(), containerId);
    }

    public SurgicalStationMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true; // For testing purposes
    }
}