package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QuestOffererPacket(int mobId) implements CustomPayload {

    public static final CustomPayload.Id<QuestOffererPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "quest_offerer_packet"));

    public static final PacketCodec<RegistryByteBuf, QuestOffererPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
    }, buf -> new QuestOffererPacket(buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

