package io.github.drag0n1zed.block.entity;

import io.github.drag0n1zed.screen.SurgicalStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SurgicalStationBlockEntity extends BlockEntity implements MenuProvider {
    public SurgicalStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SURGICAL_STATION_ENTITY.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.override.surgical_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new SurgicalStationMenu(containerId, inventory);
    }
}
