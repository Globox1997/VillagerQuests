package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CompleteQuestPacket(long questId) implements CustomPayload {

    public static final CustomPayload.Id<CompleteQuestPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "complete_quest_packet"));

    public static final PacketCodec<RegistryByteBuf, CompleteQuestPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeLong(value.questId());
    }, buf -> new CompleteQuestPacket(buf.readLong()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

