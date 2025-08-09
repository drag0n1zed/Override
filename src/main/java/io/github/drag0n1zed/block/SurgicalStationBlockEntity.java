package io.github.drag0n1zed.block;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.ai.AIGoalData;
import io.github.drag0n1zed.ai.GoalSerializer;
import io.github.drag0n1zed.ai.GoalType;
import io.github.drag0n1zed.api.OverrideRegistries;
import io.github.drag0n1zed.api.goal.IGoalRegistry;
import io.github.drag0n1zed.api.ai.validation.ValidationResult;
import io.github.drag0n1zed.block.menu.SurgicalStationMenu;
import io.github.drag0n1zed.network.ModNetworking;
import io.github.drag0n1zed.network.packet.PacketSyncAIGoals;
import io.github.drag0n1zed.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SurgicalStationBlockEntity extends BlockEntity implements MenuProvider {

    private static final String CAPTURED_ENTITY_TAG = "CapturedEntity";
    private CompoundTag capturedEntityNBT = new CompoundTag();

    public SurgicalStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SURGICAL_STATION_ENTITY.get(), pos, state);
    }

    public boolean hasCapturedEntity() {
        return !this.capturedEntityNBT.isEmpty();
    }

    public void captureEntity(Mob mob) {
        CompoundTag nbt = new CompoundTag();
        mob.saveAsPassenger(nbt);
        this.capturedEntityNBT = nbt;
        this.setChanged();
    }

    @Nullable
    public CompoundTag getCapturedEntityNBT() {
        return this.hasCapturedEntity() ? this.capturedEntityNBT : null;
    }

    public void releaseEntity() {
        this.capturedEntityNBT = new CompoundTag();
        this.setChanged();
    }

    /**
     * Validates and applies a new set of AI goals to the stored entity data.
     * This is the authoritative method for all AI modifications.
     *
     * @param goals The list of goal descriptions to apply.
     * @return true if the modification was successfully validated and applied, false otherwise.
     */
    public boolean applyAIModification(List<AIGoalData> goals) {
        if (this.level == null || this.level.isClientSide() || !this.hasCapturedEntity()) {
            return false;
        }

        IGoalRegistry registry = OverrideRegistries.getGoalRegistryProvider();
        if (registry == null) {
            OverrideMod.LOGGER.error("Cannot apply AI modification: Registry not available.");
            return false;
        }

        Optional<Entity> entityOptional = EntityType.create(this.capturedEntityNBT, this.level);

        if (entityOptional.isPresent() && entityOptional.get() instanceof Mob mob) {
            // Step 1: Validate the entire profile before applying anything.
            Map<AIGoalData, ValidationResult> validationResults = registry.validateProfile(mob, goals);
            List<String> failures = validationResults.entrySet().stream()
                    .filter(entry -> !entry.getValue().isSuccess())
                    .map(entry -> String.format("Goal '%s' failed: %s", entry.getKey().getGoalName(), String.join(", ", entry.getValue().getReasons())))
                    .toList();

            if (!failures.isEmpty()) {
                OverrideMod.LOGGER.warn("AI Profile validation failed for mob in station at {}:", getBlockPos());
                failures.forEach(failure -> OverrideMod.LOGGER.warn(" - {}", failure));
                return false; // Abort modification
            }

            // Step 2: If validation passes, apply the changes.
            GoalSerializer.saveGoalsToNBT(mob, goals);

            CompoundTag newNbt = new CompoundTag();
            mob.saveAsPassenger(newNbt);
            this.capturedEntityNBT = newNbt;

            this.setChanged();
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        if (this.hasCapturedEntity()) {
            nbt.put(CAPTURED_ENTITY_TAG, this.capturedEntityNBT);
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (nbt.contains(CAPTURED_ENTITY_TAG, CompoundTag.TAG_COMPOUND)) {
            this.capturedEntityNBT = nbt.getCompound(CAPTURED_ENTITY_TAG);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.override.surgical_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        if (this.hasCapturedEntity() && player instanceof ServerPlayer serverPlayer) {
            List<AIGoalData> goalsData = getAIGoalsFromNBT();
            ModNetworking.sendToPlayer(new PacketSyncAIGoals(goalsData), serverPlayer);
        }
        return new SurgicalStationMenu(containerId, inventory);
    }

    private List<AIGoalData> getAIGoalsFromNBT() {
        List<AIGoalData> goalsData = new ArrayList<>();
        if (this.level == null || !this.hasCapturedEntity()) {
            return goalsData;
        }

        Optional<Entity> entityOptional = EntityType.create(this.capturedEntityNBT, this.level);

        if (entityOptional.isPresent() && entityOptional.get() instanceof Mob mob) {
            // Note: This relies on the goal's SimpleName matching the registered identifier.
            // A more robust system might involve a reverse-lookup map from Goal class to identifier.
            // For now, this is consistent with the registration in VanillaGoalFactory.
            addGoalsToList(goalsData, mob.goalSelector, GoalType.GOAL);
            addGoalsToList(goalsData, mob.targetSelector, GoalType.TARGET);
        }
        return goalsData;
    }

    private void addGoalsToList(List<AIGoalData> goalsData, GoalSelector selector, GoalType type) {
        for (WrappedGoal wrappedGoal : selector.getAvailableGoals()) {
            goalsData.add(new AIGoalData(
                    type,
                    wrappedGoal.getPriority(),
                    wrappedGoal.getGoal().getClass().getSimpleName(),
                    wrappedGoal.isRunning(),
                    new CompoundTag() // Parameters are not read from existing AI, only applied
            ));
        }
    }
}