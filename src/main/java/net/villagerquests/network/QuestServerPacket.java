package net.villagerquests.network;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.libz.network.LibzServerPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.QuestData;
import net.villagerquests.gui.QuestScreenHandler;

public class QuestServerPacket {

    public static final Identifier SET_QUEST_SCREEN = new Identifier("levelz", "set_quest_screen");
    public static final Identifier SET_TRADE_SCREEN = new Identifier("levelz", "set_trade_screen");
    public static final Identifier CLOSE_SCREEN = new Identifier("levelz", "close_screen");
    public static final Identifier SET_QUEST_OFFERER = new Identifier("levelz", "set_quest_offerer");
    public static final Identifier ACCEPT_MERCHANT_QUEST = new Identifier("levelz", "accept_merchant_quest");
    public static final Identifier COMPLETE_MERCHANT_QUEST = new Identifier("levelz", "complete_merchant_quest");
    public static final Identifier QUEST_KILL_ADDITION = new Identifier("levelz", "quest_kill_addition");
    public static final Identifier QUEST_TRAVEL_ADDITION = new Identifier("levelz", "quest_travel_addition");
    public static final Identifier SYNC_PLAYER_QUEST_DATA = new Identifier("levelz", "sync_player_quest_data");
    public static final Identifier SET_MERCHANT_QUEST = new Identifier("levelz", "set_merchant_quest");
    public static final Identifier FAIL_MERCHANT_QUEST = new Identifier("levelz", "fail_merchant_quest");
    public static final Identifier REMOVE_MERCHANT_QUEST = new Identifier("levelz", "remove_merchant_quest");
    public static final Identifier DECLINE_MERCHANT_QUEST = new Identifier("levelz", "decline_merchant_quest");
    public static final Identifier QUEST_LIST_DATA = new Identifier("levelz", "quest_list_data");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(SET_QUEST_SCREEN, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                    return new QuestScreenHandler(syncId, playerInventory);
                }, Text.of("")));

                MerchantEntity merchantEntity = (MerchantEntity) player.getWorld().getEntityById(buffer.readVarInt());
                merchantEntity.setCustomer(player);
                LibzServerPacket.writeS2CMousePositionPacket(player, buffer.readInt(), buffer.readInt());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(ACCEPT_MERCHANT_QUEST, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((PlayerAccessor) player).addPlayerQuestId(buffer.readInt(), buffer.readUuid());
                Entity entity = player.getWorld().getEntityById(buffer.readInt());
                if (entity instanceof WanderingTraderEntity) {
                    ((WanderingTraderEntity) entity).setDespawnDelay(((WanderingTraderEntity) entity).getDespawnDelay() + VillagerQuestsMain.CONFIG.wanderingTraderDespawnAddition);
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_TRADE_SCREEN, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                MerchantEntity merchantEntity = (MerchantEntity) player.getWorld().getEntityById(buffer.readVarInt());
                if (merchantEntity instanceof VillagerEntity) {
                    VillagerEntity villagerEntity = (VillagerEntity) merchantEntity;
                    villagerEntity.sendOffers(player, villagerEntity.getDisplayName(), villagerEntity.getVillagerData().getLevel());
                } else {
                    merchantEntity.sendOffers(player, merchantEntity.getDisplayName(), 1);
                }
                LibzServerPacket.writeS2CMousePositionPacket(player, buffer.readInt(), buffer.readInt());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CLOSE_SCREEN, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((MerchantEntity) player.getWorld().getEntityById(buffer.readVarInt())).setCustomer(null);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(COMPLETE_MERCHANT_QUEST, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((PlayerAccessor) player).finishPlayerQuest(buffer.readInt());
                MerchantEntity merchantEntity = (MerchantEntity) player.getWorld().getEntityById(buffer.readInt());
                if (merchantEntity instanceof VillagerEntity)
                    ((MerchantAccessor) merchantEntity).finishedQuest(buffer.readInt());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(DECLINE_MERCHANT_QUEST, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((PlayerAccessor) player).failPlayerQuest(buffer.readInt(), buffer.readInt());
                MerchantEntity merchantEntity = (MerchantEntity) player.getWorld().getEntityById(buffer.readInt());
                if (player.getWorld() instanceof ServerWorld && merchantEntity instanceof VillagerEntity)
                    ((ServerWorld) player.getWorld()).handleInteraction(EntityInteraction.VILLAGER_HURT, player, (InteractionObserver) merchantEntity);
            }
        });
    }

    public static void writeS2COffererPacket(ServerPlayerEntity serverPlayerEntity, MerchantEntity merchantEntity, List<Integer> list) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeIntList(new IntArrayList(list));
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_QUEST_OFFERER, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CQuestKillAdditionPacket(ServerPlayerEntity serverPlayerEntity, int entityRawId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entityRawId);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(QUEST_KILL_ADDITION, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CQuestTravelAdditionPacket(ServerPlayerEntity serverPlayerEntity, int listRow, int listColumn) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(listRow);
        buf.writeInt(listColumn);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(QUEST_TRAVEL_ADDITION, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CPlayerQuestDataPacket(PlayerEntity playerEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        List<Integer> questList = ((PlayerAccessor) playerEntity).getPlayerQuestIdList();
        List<UUID> traderUuids = ((PlayerAccessor) playerEntity).getPlayerQuestTraderIdList();
        List<List<Integer>> killedMobQuestCount = ((PlayerAccessor) playerEntity).getPlayerKilledQuestList();
        List<List<Object>> travelIdList = ((PlayerAccessor) playerEntity).getPlayerTravelList();
        List<Integer> timerList = ((PlayerAccessor) playerEntity).getPlayerQuestTimerList();

        for (int i = 0; i < questList.size(); i++) {
            if (!QuestData.idList.contains(questList.get(i))) {
                questList.remove(i);
                traderUuids.remove(i);
                killedMobQuestCount.remove(i);
                travelIdList.remove(i);
                timerList.remove(i);
                i--;
            }
        }
        buf.writeIntList(new IntArrayList(questList));

        buf.writeInt(traderUuids.size());
        for (int i = 0; i < traderUuids.size(); i++) {
            buf.writeUuid(traderUuids.get(i));
        }

        buf.writeInt(killedMobQuestCount.size());
        for (int u = 0; u < killedMobQuestCount.size(); u++) {
            buf.writeIntList(new IntArrayList(killedMobQuestCount.get(u)));
        }

        List<Integer> finishedQuestList = ((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList();
        List<Integer> refreshList = ((PlayerAccessor) playerEntity).getPlayerQuestRefreshTimerList();

        for (int i = 0; i < finishedQuestList.size(); i++) {
            if (!QuestData.idList.contains(finishedQuestList.get(i))) {
                finishedQuestList.remove(i);
                refreshList.remove(i);
                i--;
            }
        }

        buf.writeIntList(new IntArrayList(finishedQuestList));
        buf.writeIntList(new IntArrayList(timerList));
        buf.writeIntList(new IntArrayList(refreshList));

        for (int k = 0; k < travelIdList.size(); k++) {
            if (!travelIdList.get(k).isEmpty())
                for (int u = 0; u < travelIdList.get(k).size(); u++) {
                    buf.writeString(String.valueOf(travelIdList.get(k).get(u)));
                    if (u == travelIdList.get(k).size() - 1)
                        buf.writeString("Break");
                }
            else
                buf.writeString("Null");
        }

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SYNC_PLAYER_QUEST_DATA, buf);
        ((ServerPlayerEntity) playerEntity).networkHandler.sendPacket(packet);
    }

    public static void writeS2CMerchantQuestsPacket(ServerPlayerEntity serverPlayerEntity, MerchantEntity merchantEntity, List<Integer> list) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeIntList(new IntArrayList(list));
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_MERCHANT_QUEST, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CFailQuestPacket(ServerPlayerEntity serverPlayerEntity, int questId, int reason) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        buf.writeInt(reason);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(FAIL_MERCHANT_QUEST, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CRemoveQuestPacket(ServerPlayerEntity serverPlayerEntity, int questId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(REMOVE_MERCHANT_QUEST, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void writeS2CQuestListPacket(ServerPlayerEntity serverPlayerEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        for (int i = 0; i < QuestData.getListNames().size(); i++) {
            String listName = QuestData.getListNames().get(i);
            if (listName.equals("questTaskList") || listName.equals("questRewardList")) {
                List<List<Object>> list = QuestData.getList(listName);
                buf.writeString(listName);
                for (int k = 0; k < list.size(); k++) {
                    for (int u = 0; u < list.get(k).size(); u++) {
                        buf.writeString(list.get(k).get(u).toString());
                    }
                    buf.writeString("stop");
                }
            } else {
                List list = QuestData.getList(listName);
                buf.writeString(listName);
                for (int u = 0; u < list.size(); u++) {
                    buf.writeString(list.get(u).toString());
                }
            }
        }
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(QUEST_LIST_DATA, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
