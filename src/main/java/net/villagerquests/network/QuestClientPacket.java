package net.villagerquests.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.MouseAccessor;
import net.villagerquests.accessor.PlayerAccessor;

public class QuestClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_QUEST_OFFERER, (client, handler, buf, sender) -> {
            PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
            newBuffer.writeVarInt(buf.readVarInt());
            newBuffer.writeIntList(buf.readIntList());
            client.execute(() -> {
                MerchantEntity merchantEntity = (MerchantEntity) client.world.getEntityById(newBuffer.readVarInt());
                ((MerchantAccessor) merchantEntity).setQuestIdList(newBuffer.readIntList());
                ((MerchantAccessor) client.player).setCurrentOfferer(merchantEntity);
                merchantEntity.setCurrentCustomer(client.player);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.QUEST_KILL_ADDITION, (client, handler, buf, sender) -> {
            if (client.player != null) {
                ((PlayerAccessor) client.player).canAddKilledMobQuestCount(buf.readInt());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_MOUSE_POSITION, (client, handler, buf, sender) -> {
            int mouseX = buf.readInt();
            int mouseY = buf.readInt();
            client.execute(() -> {
                ((MouseAccessor) client.mouse).setMousePosition(mouseX, mouseY);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.PLAYER_QUEST_DATA, (client, handler, buf, sender) -> {
            if (client.player != null)
                executePlayerQuestData(client.player, buf);
            else {
                PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));

                int UuidSize = buf.readInt();
                newBuffer.writeInt(UuidSize);
                for (int i = 0; i < UuidSize; i++) {
                    newBuffer.writeUuid(buf.readUuid());
                }

                int killedMobCountSize = buf.readInt();
                newBuffer.writeInt(killedMobCountSize);
                for (int u = 0; u < killedMobCountSize; u++) {
                    newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                }

                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                client.execute(() -> {
                    executePlayerQuestData(client.player, newBuffer);
                });
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_MERCHANT_QUEST, (client, handler, buf, sender) -> {
            PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
            newBuffer.writeVarInt(buf.readVarInt());
            newBuffer.writeIntList(buf.readIntList());
            client.execute(() -> {
                ((MerchantAccessor) client.world.getEntityById(newBuffer.readVarInt())).setQuestIdList(newBuffer.readIntList());
            });

        });

    }

    public static void writeC2SScreenPacket(MerchantEntity merchantEntity, int mouseX, int mouseY) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.SET_QUEST_SCREEN, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void acceptMerchantQuestC2SPacket(PlayerEntity playerEntity, int questId, UUID uuid, int entityId) {
        // Set on client
        ((PlayerAccessor) playerEntity).addPlayerQuestId(questId, uuid);

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        buf.writeUuid(uuid);
        buf.writeInt(entityId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.ACCEPT_MERCHANT_QUEST, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2STradePacket(MerchantEntity merchantEntity, int mouseX, int mouseY) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.SET_TRADE_SCREEN, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SClosePacket(MerchantEntity merchantEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.CLOSE_SCREEN, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SQuestCompletionPacket(int questId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.COMPLET_MERCHANT_QUEST, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    private static void executePlayerQuestData(PlayerEntity player, PacketByteBuf buf) {
        List<Integer> questIds = buf.readIntList();

        int UuidSize = buf.readInt();
        List<UUID> traderUuids = new ArrayList<>();
        for (int i = 0; i < UuidSize; i++) {
            traderUuids.add(buf.readUuid());
        }

        int killedMobCountSize = buf.readInt();
        List<List<Integer>> killedMobQuestCount = new ArrayList<>();
        for (int u = 0; u < killedMobCountSize; u++) {
            killedMobQuestCount.add(buf.readIntList());
        }

        List<Integer> finishedIds = buf.readIntList();
        List<Integer> timers = buf.readIntList();
        List<Integer> refreshTimers = buf.readIntList();
        ((PlayerAccessor) player).syncPlayerQuest(questIds, killedMobQuestCount, traderUuids, finishedIds, timers, refreshTimers);
    }

}
