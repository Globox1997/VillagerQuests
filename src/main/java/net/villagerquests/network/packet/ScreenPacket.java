package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ScreenPacket(int mobId, int mouseX, int mouseY, boolean villagerScreen) implements CustomPayload {

    public static final CustomPayload.Id<ScreenPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "screen_packet"));

    public static final PacketCodec<RegistryByteBuf, ScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeInt(value.mouseX());
        buf.writeInt(value.mouseY());
        buf.writeBoolean(value.villagerScreen());
    }, buf -> new ScreenPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

