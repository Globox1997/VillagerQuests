package net.villagerquests.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpMerchantScreenPacket(int mobId, boolean defaultChangeableName, boolean defaultInvincibility, boolean defaultOffersTrades) implements CustomPayload {

    public static final CustomPayload.Id<OpMerchantScreenPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("villagerquests", "op_merchant_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, OpMerchantScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mobId());
        buf.writeBoolean(value.defaultChangeableName());
        buf.writeBoolean(value.defaultInvincibility());
        buf.writeBoolean(value.defaultOffersTrades());
    }, buf -> new OpMerchantScreenPacket(buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

