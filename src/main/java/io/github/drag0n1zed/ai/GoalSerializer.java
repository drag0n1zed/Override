package io.github.drag0n1zed.ai;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.ai.factory.VanillaGoalFactory;
import io.github.drag0n1zed.api.OverrideRegistries;
import io.github.drag0n1zed.api.goal.GoalFactory;
import io.github.drag0n1zed.api.registry.IGoalRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GoalSerializer implements IGoalRegistry {

    private static final GoalSerializer INSTANCE = new GoalSerializer();

    private static final String KEY_CUSTOM_AI_FLAG = "override_ai_is_custom";
    private static final String KEY_GOAL_LIST = "override_custom_goals";
    private static final String KEY_GOAL_CLASS_NAME = "name";
    private static final String KEY_GOAL_PRIORITY = "priority";
    private static final String KEY_GOAL_TYPE = "type";

    private final Map<String, GoalFactory> factoryRegistry = new ConcurrentHashMap<>();

    private GoalSerializer() {}

    /**
     * Initializes the registry by injecting itself into the API and registering vanilla goals.
     */
    public static void initialize() {
        OverrideRegistries.setRegistryProvider(INSTANCE);
        VanillaGoalFactory.registerAll(INSTANCE);
    }

    @Override
    public void register(String goalName, GoalFactory factory) {
        factoryRegistry.put(goalName, factory);
        OverrideMod.LOGGER.debug("Registered AI Goal Factory for type '{}'", goalName);
    }

    @Override
    @Nullable
    public GoalFactory get(String goalName) {
        return factoryRegistry.get(goalName);
    }

    public static void saveGoalsToNBT(Mob mob, List<AIGoalData> goals) {
        mob.getPersistentData().putBoolean(KEY_CUSTOM_AI_FLAG, true);
        ListTag goalListTag = new ListTag();
        for (AIGoalData goalData : goals) {
            CompoundTag goalTag = new CompoundTag();
            goalTag.putString(KEY_GOAL_TYPE, goalData.type().getSerializedName());
            goalTag.putString(KEY_GOAL_CLASS_NAME, goalData.goalName());
            goalTag.putInt(KEY_GOAL_PRIORITY, goalData.priority());
            goalTag.put("data", new CompoundTag());
            goalListTag.add(goalTag);
        }
        mob.getPersistentData().put(KEY_GOAL_LIST, goalListTag);
    }

    public static void loadGoalsFromNBT(Mob mob) {
        IGoalRegistry registry = OverrideRegistries.getRegistryProvider();
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
                String goalName = goalTag.getString(KEY_GOAL_CLASS_NAME);
                int priority = goalTag.getInt(KEY_GOAL_PRIORITY);
                CompoundTag data = goalTag.getCompound("data");

                GoalFactory factory = registry.get(goalName);
                if (factory != null) {
                    try {
                        Goal goalInstance = factory.create(mob, data);
                        if (goalInstance != null) {
                            if (type == GoalType.TARGET) {
                                mob.targetSelector.addGoal(priority, goalInstance);
                            } else {
                                mob.goalSelector.addGoal(priority, goalInstance);
                            }
                        }
                    } catch (Exception e) {
                        OverrideMod.LOGGER.error("Factory for goal '{}' failed to create instance.", goalName, e);
                    }
                } else {
                    OverrideMod.LOGGER.warn("No AI Goal Factory found for type '{}'. Skipping.", goalName);
                }
            }
        }
    }
}