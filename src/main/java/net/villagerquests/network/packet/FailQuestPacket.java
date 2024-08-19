package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FailQuestPacket(long questId) implements CustomPayload {

    public static final CustomPayload.Id<FailQuestPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "fail_quest_packet"));

    public static final PacketCodec<RegistryByteBuf, FailQuestPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeLong(value.questId());
    }, buf -> new FailQuestPacket(buf.readLong()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

