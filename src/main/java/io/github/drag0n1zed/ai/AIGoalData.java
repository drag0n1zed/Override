package io.github.drag0n1zed.ai;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A Data Transfer Object (DTO) for AI Goal information.
 * It now includes a CompoundTag to hold arbitrary configuration parameters,
 * enabling customizable AI behaviors.
 */
public final class AIGoalData {

    private final GoalType type;
    private final int priority;
    private final String goalName;
    private final boolean isRunning;
    private final CompoundTag parameters;

    public static final StreamCodec<RegistryFriendlyByteBuf, AIGoalData> STREAM_CODEC = StreamCodec.composite(
            GoalType.STREAM_CODEC,
            AIGoalData::getType,
            ByteBufCodecs.INT,
            AIGoalData::getPriority,
            ByteBufCodecs.STRING_UTF8,
            AIGoalData::getGoalName,
            ByteBufCodecs.BOOL,
            AIGoalData::isRunning,
            ByteBufCodecs.COMPOUND_TAG,
            AIGoalData::getParameters,
            AIGoalData::new
    );

    /**
     * Constructs a full AIGoalData instance with parameters.
     */
    public AIGoalData(GoalType type, int priority, String goalName, boolean isRunning, CompoundTag parameters) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.priority = priority;
        this.goalName = Objects.requireNonNull(goalName, "goalName cannot be null");
        this.isRunning = isRunning;
        this.parameters = Objects.requireNonNull(parameters, "parameters cannot be null");
    }

    /**
     * Constructs an AIGoalData instance with empty parameters for simplicity.
     */
    public AIGoalData(GoalType type, int priority, String goalName, boolean isRunning) {
        this(type, priority, goalName, isRunning, new CompoundTag());
    }

    public GoalType getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public String getGoalName() {
        return goalName;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public CompoundTag getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIGoalData that = (AIGoalData) o;
        return priority == that.priority && isRunning == that.isRunning && type == that.type && goalName.equals(that.goalName) && parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, priority, goalName, isRunning, parameters);
    }

    @Override
    public String toString() {
        return "AIGoalData{" +
                "type=" + type +
                ", priority=" + priority +
                ", goalName='" + goalName + '\'' +
                ", isRunning=" + isRunning +
                ", parameters=" + parameters +
                '}';
    }
}