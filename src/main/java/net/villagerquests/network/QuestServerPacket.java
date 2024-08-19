package net.villagerquests.network;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.libz.network.LibzServerPacket;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.access.MerchantScreenHandlerAccessor;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.access.TeamDataAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.network.packet.*;
import net.villagerquests.screen.VillagerQuestScreenHandler;
import net.villagerquests.util.QuestHelper;

import java.util.*;

public class QuestServerPacket {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ScreenPacket.PACKET_ID, ScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CloseScreenPacket.PACKET_ID, CloseScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateQuestMarkPacket.PACKET_ID, UpdateQuestMarkPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(AcceptQuestPacket.PACKET_ID, AcceptQuestPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CompleteQuestPacket.PACKET_ID, CompleteQuestPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CompleteTalkQuestPacket.PACKET_ID, CompleteTalkQuestPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(OpMerchantPacket.PACKET_ID, OpMerchantPacket.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(QuestOffererPacket.PACKET_ID, QuestOffererPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestMarkPacket.PACKET_ID, QuestMarkPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(FailQuestPacket.PACKET_ID, FailQuestPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(OffersTradesPacket.PACKET_ID, OffersTradesPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestTalkPacket.PACKET_ID, QuestTalkPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(OpMerchantScreenPacket.PACKET_ID, OpMerchantScreenPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ScreenPacket.PACKET_ID, (payload, context) -> {
            int villagerId = payload.mobId();
            int mouseX = payload.mouseX();
            int mouseY = payload.mouseY();
            Boolean questScreen = payload.villagerScreen();
            context.server().execute(() -> {
                if (context.player().currentScreenHandler instanceof MerchantScreenHandlerAccessor) {
                    ((MerchantScreenHandlerAccessor) context.player().currentScreenHandler).setSwitchingScreen(true);
                }
                if (questScreen) {
                    context.player().openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                        return new VillagerQuestScreenHandler(syncId, playerInventory);
                    }, Text.of("")));
                } else {
                    MerchantEntity merchantEntity = (MerchantEntity) context.player().getWorld().getEntityById(villagerId);
                    if (merchantEntity instanceof VillagerEntity) {
                        VillagerEntity villagerEntity = (VillagerEntity) merchantEntity;
                        villagerEntity.sendOffers(context.player(), villagerEntity.getDisplayName(), villagerEntity.getVillagerData().getLevel());
                    } else {
                        merchantEntity.sendOffers(context.player(), merchantEntity.getDisplayName(), 1);
                    }
                }
                LibzServerPacket.writeS2CMousePositionPacket(context.player(), mouseX, mouseY);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CloseScreenPacket.PACKET_ID, (payload, context) -> {
            int merchantEntityId = payload.mobId();
            context.server().execute(() -> {
                if (context.player().getServerWorld().getEntityById(merchantEntityId) instanceof MerchantEntity merchantEntity) {
                    merchantEntity.setCustomer(null);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateQuestMarkPacket.PACKET_ID, (payload, context) -> {
            UUID villagerUuid = payload.uuid();
            context.server().execute(() -> {
                // Sync all online players
                List<ServerPlayerEntity> list = context.server().getPlayerManager().getPlayerList();
                for (int i = 0; i < list.size(); i++) {
                    int questMarkType = -1;
                    if (list.get(i).getServerWorld().getEntity(villagerUuid) != null) {
                        questMarkType = QuestHelper.getVillagerQuestMarkType(list.get(i), villagerUuid);
                        writeS2CMerchantQuestMarkPacket(list.get(i), list.get(i).getServerWorld().getEntity(villagerUuid).getId(), questMarkType);
                    }
                    VillagerQuestState.updatePlayerVillagerQuestMarkType(context.server(), list.get(i).getUuid(), villagerUuid, questMarkType);
                }
                // Sync all offline players
                Iterator<UUID> playerUuids = VillagerQuestState.getPlayerVillagerQuestState(context.server(), villagerUuid).getMerchantQuestMarkMap().keySet().iterator();
                while (playerUuids.hasNext()) {
                    UUID playerUuid = playerUuids.next();
                    if (context.server().getPlayerManager().getPlayer(playerUuid) == null) {
                        VillagerQuestState.getPlayerVillagerQuestState(context.server(), playerUuid).getMerchantQuestMarkMap().put(villagerUuid, -1);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(AcceptQuestPacket.PACKET_ID, (payload, context) -> {
            long questId = payload.questId();
            boolean acceptQuest = payload.acceptQuest();
            context.server().execute(() -> {
                Quest quest = ServerQuestFile.INSTANCE.getQuest(questId);
                TeamData teamData = TeamData.get(context.player());
                if ((Object) quest instanceof QuestAccessor questAccessor && questAccessor.isQuestVisible(teamData)) {
                    ((TeamDataAccessor) teamData).setQuestStarted(questId, acceptQuest ? new Date() : null);
                    int questMarkType = QuestHelper.getVillagerQuestMarkType(context.player(), questAccessor.getVillagerQuestUuid());
                    Iterator<UUID> iterator = FTBTeamsAPI.api().getManager().getTeamByID(teamData.getTeamId()).get().getMembers().iterator();
                    while (iterator.hasNext()) {
                        UUID uuid = iterator.next();
                        if (context.server().getPlayerManager().getPlayer(uuid) != null) {
                            if (context.server().getPlayerManager().getPlayer(uuid).getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                                writeS2CMerchantQuestMarkPacket(context.server().getPlayerManager().getPlayer(uuid), merchantEntity.getId(), questMarkType);
                            }
                        }
                        VillagerQuestState.updatePlayerVillagerQuestMarkType(context.server(), uuid, questAccessor.getVillagerQuestUuid(), questMarkType);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CompleteQuestPacket.PACKET_ID, (payload, context) -> {
            long questId = payload.questId();
            context.server().execute(() -> {
                Quest quest = ServerQuestFile.INSTANCE.getQuest(questId);
                TeamData data = TeamData.get(context.player());
                if ((Object) quest instanceof QuestAccessor questAccessor && questAccessor.isQuestVisible(data) && questAccessor.isVillagerQuest()) {
                    if (quest.isCompletedRaw(data) && !data.isCompleted(quest)) {
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

                        QuestHelper.updateTeamQuestMark(context.server(), data, questAccessor.getVillagerQuestUuid());
                    } else {
                        int questMarkType = QuestHelper.getVillagerQuestMarkType(context.player(), questAccessor.getVillagerQuestUuid());
                        if (context.player().getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                            writeS2CMerchantQuestMarkPacket(context.player(), merchantEntity.getId(), questMarkType);
                        }
                        VillagerQuestState.updatePlayerVillagerQuestMarkType(context.server(), context.player().getUuid(), questAccessor.getVillagerQuestUuid(), questMarkType);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CompleteTalkQuestPacket.PACKET_ID, (payload, context) -> {
            int merchantEntityId = payload.mobId();
            long questId = payload.questId();
            context.server().execute(() -> {
                if (context.player().getServerWorld().getEntityById(merchantEntityId) instanceof MerchantEntity merchantEntity) {
                    ServerQuestFile.INSTANCE.getQuest(questId).getTasks().forEach(task -> {
                        if (task instanceof VillagerTalkTask villagerTalkTask) {
                            villagerTalkTask.talk(TeamData.get(context.player()), merchantEntity);
                            merchantEntity.setCustomer(null);
                        }
                    });
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(OpMerchantPacket.PACKET_ID, (payload, context) -> {
            int id = payload.mobId();
            String merchantName = payload.merchantName();
            boolean changeableName = payload.changeableName();
            boolean invincibility = payload.invincibility();
            boolean hasAi = payload.hasAi();
            boolean offersTrades = payload.offersTrades();

            context.server().execute(() -> {
                if (context.player().isCreativeLevelTwoOp() && context.player().getServerWorld().getEntityById(id) instanceof MerchantEntity merchantEntity) {
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
    }

    public static void writeS2COffererPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId) {
        ServerPlayNetworking.send(serverPlayerEntity, new QuestOffererPacket(merchantEntityId));
    }

    public static void writeS2CMerchantQuestMarkPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId, int questMarkType) {
        ServerPlayNetworking.send(serverPlayerEntity, new QuestMarkPacket(merchantEntityId, questMarkType));
    }

    public static void writeS2CFailQuestPacket(ServerPlayerEntity serverPlayerEntity, long questId) {
        ServerPlayNetworking.send(serverPlayerEntity, new FailQuestPacket(questId));
    }

    public static void writeS2COffersTradesPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId, boolean offersTrades) {
        ServerPlayNetworking.send(serverPlayerEntity, new OffersTradesPacket(merchantEntityId, offersTrades));
    }

    public static void writeS2CTalkPacket(ServerPlayerEntity serverPlayerEntity, int merchantEntityId, long questId) {
        ServerPlayNetworking.send(serverPlayerEntity, new QuestTalkPacket(merchantEntityId, questId));
    }

    public static void writeS2COpMerchantScreenPacket(ServerPlayerEntity serverPlayerEntity, MerchantEntity merchantEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, new OpMerchantScreenPacket(merchantEntity.getId(), ((MerchantAccessor) merchantEntity).getChangeableName(), merchantEntity.isInvulnerable(), ((MerchantAccessor) merchantEntity).getOffersTrades()));
    }


}
