package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QuestTalkPacket(int mobId, long questId) implements CustomPayload {

    public static final CustomPayload.Id<QuestTalkPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "quest_talk_packet"));

    public static final PacketCodec<RegistryByteBuf, QuestTalkPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeLong(value.questId());
    }, buf -> new QuestTalkPacket(buf.readInt(), buf.readLong()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

