package io.github.drag0n1zed.ai;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.ai.goal.factory.VanillaGoalFactory;
import io.github.drag0n1zed.api.OverrideRegistries;
import io.github.drag0n1zed.api.ai.GoalDefinition;
import io.github.drag0n1zed.api.goal.IGoalRegistry;
import io.github.drag0n1zed.api.ai.validation.ValidationResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class GoalSerializer implements IGoalRegistry {

    private static final GoalSerializer INSTANCE = new GoalSerializer();

    private static final String KEY_CUSTOM_AI_FLAG = "override_ai_is_custom";
    private static final String KEY_GOAL_LIST = "override_custom_goals";
    private static final String KEY_GOAL_ID = "id";
    private static final String KEY_GOAL_PRIORITY = "priority";
    private static final String KEY_GOAL_TYPE = "type";

    private final Map<String, GoalDefinition> definitionRegistry = new ConcurrentHashMap<>();

    private GoalSerializer() {}

    /**
     * Initializes the registry by injecting itself into the API and registering vanilla goals.
     */
    public static void initialize() {
        OverrideRegistries.setGoalRegistryProvider(INSTANCE);
        VanillaGoalFactory.registerAll(INSTANCE);
    }

    @Override
    public void register(GoalDefinition definition) {
        definitionRegistry.put(definition.identifier(), definition);
        OverrideMod.LOGGER.debug("Registered AI Goal Definition for identifier '{}'", definition.identifier());
    }

    @Override
    public Optional<GoalDefinition> getDefinition(String identifier) {
        return Optional.ofNullable(definitionRegistry.get(identifier));
    }

    @Override
    public List<GoalDefinition> getAllDefinitions() {
        return List.copyOf(definitionRegistry.values());
    }

    @Override
    public List<GoalDefinition> getCompatibleDefinitions(Mob mob) {
        return definitionRegistry.values().stream()
                .filter(def -> def.validator().apply(mob).isSuccess())
                .collect(Collectors.toList());
    }

    @Override
    public Map<AIGoalData, ValidationResult> validateProfile(Mob mob, List<AIGoalData> profile) {
        return profile.stream()
                .collect(Collectors.toMap(
                        goalData -> goalData,
                        goalData -> getDefinition(goalData.getGoalName())
                                .map(def -> def.validator().apply(mob))
                                .orElse(ValidationResult.failure("Unknown goal identifier: " + goalData.getGoalName()))
                ));
    }

    public static void saveGoalsToNBT(Mob mob, List<AIGoalData> goals) {
        mob.getPersistentData().putBoolean(KEY_CUSTOM_AI_FLAG, true);
        ListTag goalListTag = new ListTag();
        for (AIGoalData goalData : goals) {
            CompoundTag goalTag = new CompoundTag();
            goalTag.putString(KEY_GOAL_TYPE, goalData.getType().getSerializedName());
            goalTag.putString(KEY_GOAL_ID, goalData.getGoalName());
            goalTag.putInt(KEY_GOAL_PRIORITY, goalData.getPriority());
            goalTag.put("data", goalData.getParameters()); // Save the configurable parameters
            goalListTag.add(goalTag);
        }
        mob.getPersistentData().put(KEY_GOAL_LIST, goalListTag);
    }

    public static void loadGoalsFromNBT(Mob mob) {
        IGoalRegistry registry = OverrideRegistries.getGoalRegistryProvider();
        if (registry == null) {
            OverrideMod.LOGGER.error("Override registry provider not found. AI cannot be loaded.");
            return;
        }

        if (!mob.getPersistentData().getBoolean(KEY_CUSTOM_AI_FLAG)) {
            return;
        }

        mob.goalSelector.getAvailableGoals().clear();
        mob.targetSelector.getAvailableGoals().clear();
        OverrideMod.LOGGER.debug("Cleared default AI for mob {}", mob.getUUID());

        ListTag goalListTag = mob.getPersistentData().getList(KEY_GOAL_LIST, Tag.TAG_COMPOUND);
        for (Tag tag : goalListTag) {
            if (tag instanceof CompoundTag goalTag) {
                GoalType type = GoalType.fromString(goalTag.getString(KEY_GOAL_TYPE), GoalType.GOAL);
                String goalId = goalTag.getString(KEY_GOAL_ID);
                int priority = goalTag.getInt(KEY_GOAL_PRIORITY);
                CompoundTag data = goalTag.getCompound("data");

                registry.getDefinition(goalId).ifPresentOrElse(definition -> {
                    try {
                        Goal goalInstance = definition.factory().create(mob, data);
                        if (goalInstance != null) {
                            if (type == GoalType.TARGET) {
                                mob.targetSelector.addGoal(priority, goalInstance);
                            } else {
                                mob.goalSelector.addGoal(priority, goalInstance);
                            }
                        } else {
                            OverrideMod.LOGGER.warn("Factory for goal '{}' returned null. This may be due to a failed type check inside the factory.", goalId);
                        }
                    } catch (Exception e) {
                        OverrideMod.LOGGER.error("Factory for goal '{}' threw an exception during creation.", goalId, e);
                    }
                }, () -> {
                    OverrideMod.LOGGER.warn("No AI Goal Definition found for identifier '{}'. Skipping.", goalId);
                });
            }
        }
    }
}