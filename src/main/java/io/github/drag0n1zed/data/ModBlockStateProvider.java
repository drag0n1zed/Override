package io.github.drag0n1zed.data;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OverrideMod.MODID, exFileHelper);
    }

    /**
     * This is where we define all of our block states and models.
     */
    @Override
    protected void registerStatesAndModels() {
        // Target the Surgical Station block for model generation.
        Block surgicalStation = ModBlocks.SURGICAL_STATION.get();

        // As noted in the documentation, we can use the models() helper to create a model file.
        // We will create a simple cube model that uses the vanilla smithing table-top texture on all sides.
        // The getBuilder("surgical_station") call is implied by cubeAll's first argument.
        ModelFile surgicalStationModel = models().cubeAll("surgical_station",
                mcLoc("block/smithing_table_top"));

        simpleBlockWithItem(surgicalStation, surgicalStationModel);
    }
}