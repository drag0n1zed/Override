package io.github.drag0n1zed.block.entity;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, OverrideMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SurgicalStationBlockEntity>> SURGICAL_STATION_ENTITY =
            BLOCK_ENTITIES.register("surgical_station_entity", () ->
                    BlockEntityType.Builder.of(SurgicalStationBlockEntity::new,
                            ModBlocks.SURGICAL_STATION.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
