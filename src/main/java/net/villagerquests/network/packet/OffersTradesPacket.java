package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OffersTradesPacket(int mobId, boolean offersTrades) implements CustomPayload {

    public static final CustomPayload.Id<OffersTradesPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "offers_trades_packet"));

    public static final PacketCodec<RegistryByteBuf, OffersTradesPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeBoolean(value.offersTrades());
    }, buf -> new OffersTradesPacket(buf.readInt(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

