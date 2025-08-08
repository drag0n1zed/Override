package io.github.drag0n1zed.network;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.network.packet.PacketSyncAIGoals;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@EventBusSubscriber(modid = OverrideMod.MODID)
public class ModNetworking {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OverrideMod.MODID);

        // Correctly register the packet payload with its type, codec, and handler
        registrar.playToClient(
                PacketSyncAIGoals.TYPE,
                PacketSyncAIGoals.STREAM_CODEC,
                PacketSyncAIGoals.Handler::handle
        );
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}