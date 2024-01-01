package net.villagerquests.network;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.libz.network.LibzServerPacket;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.access.MerchantScreenHandlerAccessor;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.screen.VillagerQuestScreenHandler;
import net.villagerquests.util.QuestHelper;

public class QuestServerPacket {

    public static final Identifier SET_SCREEN = new Identifier("villagerquest", "set_screen");
    public static final Identifier SET_QUEST_OFFERER = new Identifier("villagerquest", "set_quest_offerer");
    public static final Identifier SET_MERCHANT_QUEST_MARK = new Identifier("villagerquest", "set_merchant_quest_mark");

    public static final Identifier UPDATE_MERCHANT_QUEST_MARK = new Identifier("villagerquest", "update_merchant_quest_mark");

    public static final Identifier ACCEPT_QUEST = new Identifier("villagerquest", "accept_quest");
    public static final Identifier COMPLETE_QUEST = new Identifier("villagerquest", "complete_quest");
    public static final Identifier OFFERS_TRADES = new Identifier("villagerquest", "offers_trades");
    public static final Identifier TALK = new Identifier("villagerquest", "talk");
    public static final Identifier COMPLETE_TALK = new Identifier("villagerquest", "complete_talk");

    public static final Identifier OP_MERCHANT_SCREEN_PACKET = new Identifier("villagerquest", "op_merchant_screen");
    public static final Identifier OP_MERCHANT_PACKET = new Identifier("villagerquest", "op_merchant");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(SET_SCREEN, (server, player, handler, buffer, sender) -> {
            int villagerId = buffer.readInt();
            int mouseX = buffer.readInt();
            int mouseY = buffer.readInt();
            Boolean questScreen = buffer.readBoolean();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof MerchantScreenHandlerAccessor) {
                    ((MerchantScreenHandlerAccessor) player.currentScreenHandler).setSwitchingScreen(true);
                }
                if (questScreen) {
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                        return new VillagerQuestScreenHandler(syncId, playerInventory);
                    }, Text.of("")));
                } else {
                    MerchantEntity merchantEntity = (MerchantEntity) player.getWorld().getEntityById(villagerId);
                    if (merchantEntity instanceof VillagerEntity) {
                        VillagerEntity villagerEntity = (VillagerEntity) merchantEntity;
                        villagerEntity.sendOffers(player, villagerEntity.getDisplayName(), villagerEntity.getVillagerData().getLevel());
                    } else {
                        merchantEntity.sendOffers(player, merchantEntity.getDisplayName(), 1);
                    }
                }
                LibzServerPacket.writeS2CMousePositionPacket(player, mouseX, mouseY);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ACCEPT_QUEST, (server, player, handler, buffer, sender) -> {
            long questId = buffer.readLong();
            boolean acceptQuest = buffer.readBoolean();
            server.execute(() -> {
                Quest quest = ServerQuestFile.INSTANCE.getQuest(questId);
                if ((Object) quest instanceof QuestAccessor questAccessor && questAccessor.isQuestVisible(TeamData.get(player))) {
                    questAccessor.setAccepted(acceptQuest);

                    int questMarkType = QuestHelper.getVillagerQuestMarkType(player, questAccessor.getVillagerQuestUuid());
                    VillagerQuestState.updatePlayerVillagerQuestMarkType(player, questAccessor.getVillagerQuestUuid(), questMarkType);
                    if (player.getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                        writeS2CMerchantQuestMarkPacket(player, merchantEntity.getId(), questMarkType);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(COMPLETE_QUEST, (server, player, handler, buffer, sender) -> {
            long questId = buffer.readLong();
            server.execute(() -> {
                Quest quest = ServerQuestFile.INSTANCE.getQuest(questId);
                TeamData data = TeamData.get(player);
                if ((Object) quest instanceof QuestAccessor questAccessor && questAccessor.isQuestVisible(data) && questAccessor.isVillagerQuest() && quest.isCompletedRaw(data)) {
                    Collection<ServerPlayerEntity> onlineMembers = data.getOnlineMembers();
                    Collection<ServerPlayerEntity> notifiedPlayers;

                    if (QuestObjectBase.shouldSendNotifications()) {
                        notifiedPlayers = onlineMembers;
                    } else {
                        notifiedPlayers = List.of();
                    }
                    @SuppressWarnings("rawtypes")
                    QuestProgressEventData questProgressEventData = new QuestProgressEventData<>(new Date(), data, quest, onlineMembers, notifiedPlayers);
                    quest.onCompleted(questProgressEventData);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(OP_MERCHANT_PACKET, (server, player, handler, buffer, sender) -> {
            int id = buffer.readInt();
            String merchantName = buffer.readString();
            boolean changeableName = buffer.readBoolean();
            boolean invincibility = buffer.readBoolean();
            boolean hasAi = buffer.readBoolean();
            boolean offersTrades = buffer.readBoolean();

            server.execute(() -> {
                if (player.isCreativeLevelTwoOp() && player.getServerWorld().getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    if (!merchantEntity.getName().getString().equals(merchantName)) {
                        ((MerchantAccessor) merchantEntity).setChangeableName(true);
                        merchantEntity.setCustomName(Text.literal(merchantName));
                    }
                    ((MerchantAccessor) merchantEntity).setChangeableName(changeableName);
                    merchantEntity.setInvulnerable(invincibility);
                    merchantEntity.setAiDisabled(!hasAi);
                    ((MerchantAccessor) merchantEntity).setOffersTrades(offersTrades);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_MERCHANT_QUEST_MARK, (server, player, handler, buffer, sender) -> {
            UUID uuid = buffer.readUuid();
            server.execute(() -> {
                if (player.getServerWorld().getEntity(uuid) instanceof MerchantEntity merchantEntity) {
                    server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                        int questMarkType = QuestHelper.getVillagerQuestMarkType(player, uuid);
                        VillagerQuestState.updatePlayerVillagerQuestMarkType(player, uuid, questMarkType);
                    });
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(COMPLETE_TALK, (server, player, handler, buffer, sender) -> {
            int merchantEntityId = buffer.readInt();
            long questId = buffer.readLong();
            server.execute(() -> {
                if (player.getServerWorld().getEntityById(merchantEntityId) instanceof MerchantEntity merchantEntity) {
                    ServerQuestFile.INSTANCE.getQuest(questId).getTasks().forEach(task -> {
                        if (task instanceof VillagerTalkTask villagerTalkTask) {
                            villagerTalkTask.talk(TeamData.get(player), merchantEntity);
                        }
                    });
                }
            });
        });
    }

    public static void writeS2COffererPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntityId);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_QUEST_OFFERER, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CMerchantQuestMarkPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId, int questMarkType) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntityId);
        buf.writeInt(questMarkType);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SET_MERCHANT_QUEST_MARK, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2COpMerchantScreenPacket(ServerPlayerEntity serverPlayerEntity, MerchantEntity merchantEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntity.getId());
        buf.writeBoolean(((MerchantAccessor) merchantEntity).getChangeableName());
        buf.writeBoolean(merchantEntity.isInvulnerable());
        buf.writeBoolean(((MerchantAccessor) merchantEntity).getOffersTrades());
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(OP_MERCHANT_SCREEN_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2COffersTradesPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId, boolean offersTrades) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntityId);
        buf.writeBoolean(offersTrades);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(OFFERS_TRADES, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CTalkPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId, long questId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntityId);
        buf.writeLong(questId);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(TALK, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
