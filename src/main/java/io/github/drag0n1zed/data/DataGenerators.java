package io.github.drag0n1zed.data;

import io.github.drag0n1zed.OverrideMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = OverrideMod.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Add our block state and model provider.
        // This provider will be responsible for creating blockstates, block models, and item models for our blocks.
        generator.addProvider(
                event.includeClient(), // Only run this for the client data generator
                new ModBlockStateProvider(packOutput, existingFileHelper)
        );
    }
}