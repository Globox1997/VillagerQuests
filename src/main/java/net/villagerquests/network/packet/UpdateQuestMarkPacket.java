package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record UpdateQuestMarkPacket(UUID uuid) implements CustomPayload {

    public static final CustomPayload.Id<UpdateQuestMarkPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "update_quest_mark_packet"));

    public static final PacketCodec<RegistryByteBuf, UpdateQuestMarkPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeUuid(value.uuid());
    }, buf -> new UpdateQuestMarkPacket(buf.readUuid()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

