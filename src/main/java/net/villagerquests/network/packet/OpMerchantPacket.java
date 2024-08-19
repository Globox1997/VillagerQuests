package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpMerchantPacket(int mobId, String merchantName, boolean changeableName, boolean invincibility, boolean hasAi, boolean offersTrades) implements CustomPayload {

    public static final CustomPayload.Id<OpMerchantPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "op_merchant_packet"));

    public static final PacketCodec<RegistryByteBuf, OpMerchantPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeString(value.merchantName());
        buf.writeBoolean(value.changeableName());
        buf.writeBoolean(value.invincibility());
        buf.writeBoolean(value.hasAi());
        buf.writeBoolean(value.offersTrades());
    }, buf -> new OpMerchantPacket(buf.readInt(), buf.readString(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

