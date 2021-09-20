package net.villagerquests.network;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.gui.QuestScreenHandler;

public class QuestServerPacket {

    public static final Identifier SET_QUEST_SCREEN = new Identifier("levelz", "set_quest_screen");
    public static final Identifier SET_TRADE_SCREEN = new Identifier("levelz", "set_trade_screen");
    public static final Identifier CLOSE_SCREEN = new Identifier("levelz", "close_screen");
    public static final Identifier SET_QUEST_OFFERER = new Identifier("levelz", "set_quest_offerer");
    public static final Identifier ACCEPT_MERCHANT_QUEST = new Identifier("levelz", "accept_merchant_quest");
    public static final Identifier COMPLET_MERCHANT_QUEST = new Identifier("levelz", "complet_merchant_quest");
    public static final Identifier QUEST_KILL_ADDITION = new Identifier("levelz", "quest_kill_addition");
    public static final Identifier SET_MOUSE_POSITION = new Identifier("levelz", "set_mouse_position");
    public static final Identifier PLAYER_QUEST_DATA = new Identifier("levelz", "player_quest_data");
    public static final Identifier SET_MERCHANT_QUEST = new Identifier("levelz", "set_merchant_quest");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(SET_QUEST_SCREEN, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                MerchantScreenHandler merchantScreenHandler = (MerchantScreenHandler) player.currentScreenHandler;
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                    return new QuestScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY, merchantScreenHandler);
                }, Text.of("")));

                MerchantEntity merchantEntity = (MerchantEntity) player.world.getEntityById(buffer.readVarInt());
                merchantEntity.setCurrentCustomer(player);
                // Send back to make sure customer is set on client
                // Maybe unnecessary
                // writeS2COffererPacket(player, merchantEntity, ((MerchantAccessor) merchantEntity).getQuestIdList());
                writeS2CMousePositionPacket(player, buffer.readInt(), buffer.readInt());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(ACCEPT_MERCHANT_QUEST, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((PlayerAccessor) player).addPlayerQuestId(buffer.readInt(), buffer.readUuid());
                Entity entity = player.world.getEntityById(buffer.readInt());
                if (entity instanceof WanderingTraderEntity) {
                    ((WanderingTraderEntity) entity).setDespawnDelay(((WanderingTraderEntity) entity).getDespawnDelay() + VillagerQuestsMain.CONFIG.wanderingTraderDespawnAddition);
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_TRADE_SCREEN, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                MerchantEntity merchantEntity = (MerchantEntity) player.world.getEntityById(buffer.readVarInt());
                if (merchantEntity instanceof VillagerEntity) {
                    VillagerEntity villagerEntity = (VillagerEntity) merchantEntity;
                    villagerEntity.sendOffers(player, villagerEntity.getDisplayName(), villagerEntity.getVillagerData().getLevel());
                } else {
                    merchantEntity.sendOffers(player, merchantEntity.getDisplayName(), 1);
                }
                writeS2CMousePositionPacket(player, buffer.readInt(), buffer.readInt());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CLOSE_SCREEN, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((MerchantEntity) player.world.getEntityById(buffer.readVarInt())).setCurrentCustomer(null);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(COMPLET_MERCHANT_QUEST, (server, player, handler, buffer, sender) -> {
            if (player != null) {
                ((PlayerAccessor) player).finishPlayerQuest(buffer.readInt());
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

    public static void writeS2CPlayerQuestDataPacket(PlayerEntity playerEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeIntList(new IntArrayList(((PlayerAccessor) playerEntity).getPlayerQuestIdList()));
        List<UUID> traderUuids = ((PlayerAccessor) playerEntity).getPlayerQuestTraderIdList();
        buf.writeInt(traderUuids.size());
        for (int i = 0; i < traderUuids.size(); i++) {
            buf.writeUuid(traderUuids.get(i));
        }

        List<List<Integer>> killedMobQuestCount = ((PlayerAccessor) playerEntity).getPlayerKilledQuestList();
        buf.writeInt(killedMobQuestCount.size());
        for (int u = 0; u < killedMobQuestCount.size(); u++) {
            buf.writeIntList(new IntArrayList(killedMobQuestCount.get(u)));
        }

        buf.writeIntList(new IntArrayList(((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList()));
        buf.writeIntList(new IntArrayList(((PlayerAccessor) playerEntity).getPlayerQuestTimerList()));
        buf.writeIntList(new IntArrayList(((PlayerAccessor) playerEntity).getPlayerQuestRefreshTimerList()));

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(PLAYER_QUEST_DATA, buf);
        ((ServerPlayerEntity) playerEntity).networkHandler.sendPacket(packet);
    }

    private static void writeS2CMousePositionPacket(ServerPlayerEntity serverPlayerEntity, int mouseX, int mouseY) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_MOUSE_POSITION, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CMerchantQuestsPacket(ServerPlayerEntity serverPlayerEntity, MerchantEntity merchantEntity, List<Integer> list) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeIntList(new IntArrayList(list));
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_MERCHANT_QUEST, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
