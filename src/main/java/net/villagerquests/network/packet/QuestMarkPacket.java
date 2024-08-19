package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QuestMarkPacket(int mobId, int questMarkType) implements CustomPayload {

    public static final CustomPayload.Id<QuestMarkPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "quest_mark_packet"));

    public static final PacketCodec<RegistryByteBuf, QuestMarkPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeInt(value.questMarkType());
    }, buf -> new QuestMarkPacket(buf.readInt(),buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

