package io.github.drag0n1zed.ai;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A Data Transfer Object (DTO) for sending AI Goal information to the client.
 * Now includes the type of goal (standard or target).
 */
public record AIGoalData(GoalType type, int priority, String goalName, boolean isRunning) {

    public static final StreamCodec<RegistryFriendlyByteBuf, AIGoalData> STREAM_CODEC = StreamCodec.composite(
            GoalType.STREAM_CODEC,
            AIGoalData::type,
            ByteBufCodecs.INT,
            AIGoalData::priority,
            ByteBufCodecs.STRING_UTF8,
            AIGoalData::goalName,
            ByteBufCodecs.BOOL,
            AIGoalData::isRunning,
            AIGoalData::new
    );
}