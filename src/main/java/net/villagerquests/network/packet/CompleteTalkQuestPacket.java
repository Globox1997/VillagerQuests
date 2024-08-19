package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CompleteTalkQuestPacket(int mobId, long questId) implements CustomPayload {

    public static final CustomPayload.Id<CompleteTalkQuestPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "complete_talk_quest_packet"));

    public static final PacketCodec<RegistryByteBuf, CompleteTalkQuestPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeLong(value.questId());
    }, buf -> new CompleteTalkQuestPacket(buf.readInt(), buf.readLong()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

