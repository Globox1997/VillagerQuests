package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CloseScreenPacket(int mobId) implements CustomPayload {

    public static final CustomPayload.Id<CloseScreenPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "close_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, CloseScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
    }, buf -> new CloseScreenPacket(buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

