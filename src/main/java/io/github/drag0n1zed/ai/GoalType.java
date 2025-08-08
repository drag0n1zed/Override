package io.github.drag0n1zed.ai;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public enum GoalType implements StringRepresentable {
    GOAL("goal"),
    TARGET("target");

    public static final Codec<GoalType> CODEC = StringRepresentable.fromEnum(GoalType::values);
    public static final IntFunction<GoalType> BY_ID = ByIdMap.continuous(GoalType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, GoalType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GoalType::ordinal);

    private final String name;

    GoalType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    /**
     * Safely deserializes a GoalType from a string, returning a fallback if not found.
     * @param name The serialized name of the goal type.
     * @param fallback The value to return if the name is not found.
     * @return The corresponding GoalType or the fallback.
     */
    public static GoalType fromString(String name, GoalType fallback) {
        for (GoalType type : values()) {
            if (type.getSerializedName().equals(name)) {
                return type;
            }
        }
        return fallback;
    }
}