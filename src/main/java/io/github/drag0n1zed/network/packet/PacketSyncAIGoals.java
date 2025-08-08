package io.github.drag0n1zed.network.packet;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.ai.AIGoalData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketSyncAIGoals(List<AIGoalData> goals) implements CustomPacketPayload {

    public static final Type<PacketSyncAIGoals> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(OverrideMod.MODID, "sync_ai_goals"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncAIGoals> STREAM_CODEC = StreamCodec.composite(
            AIGoalData.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()),
            PacketSyncAIGoals::goals,
            PacketSyncAIGoals::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler {
        public static void handle(final PacketSyncAIGoals message, final IPayloadContext context) {
            context.enqueueWork(() -> {
                OverrideMod.LOGGER.info("Received AI Goal data from server:");
                message.goals().forEach(goal -> {
                    // Corrected the formatting string to be a single, valid line.
                    OverrideMod.LOGGER.info("  - [{}] {} (Running: {})", goal.priority(), goal.goalName(), goal.isRunning());
                });
            });
        }
    }
}