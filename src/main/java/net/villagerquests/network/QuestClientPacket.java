package net.villagerquests.network;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.passive.MerchantEntity;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.ftb.FailQuestToast;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.network.packet.*;
import net.villagerquests.screen.VillagerQuestOpScreen;
import net.villagerquests.screen.VillagerQuestTalkScreen;

import java.util.Iterator;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class QuestClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(QuestOffererPacket.PACKET_ID, (payload, context) -> {
            int id = payload.mobId();
            context.client().execute(() -> {
                if (context.client().world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    merchantEntity.setCustomer(context.player());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(QuestMarkPacket.PACKET_ID, (payload, context) -> {
            int id = payload.mobId();
            int questMarkType = payload.questMarkType();
            context.client().execute(() -> {
                if (context.client().world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    ((MerchantAccessor) merchantEntity).setQuestMarkType(questMarkType);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FailQuestPacket.PACKET_ID, (payload, context) -> {
            long questId = payload.questId();
            context.client().execute(() -> {
                QuestObject object = ClientQuestFile.INSTANCE.get(questId);
                if (object != null) {
                    context.client().getToastManager().add(new FailQuestToast(object));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OffersTradesPacket.PACKET_ID, (payload, context) -> {
            int id = payload.mobId();
            boolean offersTrades = payload.offersTrades();
            context.client().execute(() -> {
                if (context.client().world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    ((MerchantAccessor) merchantEntity).setOffersTrades(offersTrades);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(QuestTalkPacket.PACKET_ID, (payload, context) -> {
            int id = payload.mobId();
            long questId = payload.questId();
            context.client().execute(() -> {
                if (context.client().world.getEntityById(id) instanceof MerchantEntity merchantEntity && ClientQuestFile.INSTANCE.get(questId) instanceof Quest quest) {
                    Iterator<Task> iterator = quest.getTasks().iterator();
                    TeamData teamData = ClientQuestFile.INSTANCE.selfTeamData;
                    while (iterator.hasNext()) {
                        Task task = iterator.next();
                        if (task instanceof VillagerTalkTask villagerTalkTask && teamData.getProgress(task) < task.getMaxProgress() && teamData.canStartTasks(task.getQuest())) {
                            context.client().setScreen(new VillagerQuestTalkScreen(merchantEntity, questId, villagerTalkTask.getTalkTextList().stream().map(line -> TextUtils.parseRawText(line, task.getQuest().holderLookup())).toList()));
                        }
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpMerchantScreenPacket.PACKET_ID, (payload, context) -> {
            int id = payload.mobId();
            boolean defaultChangeableName = payload.defaultChangeableName();
            boolean defaultInvincibility = payload.defaultInvincibility();
            boolean defaultOffersTrades = payload.defaultOffersTrades();

            context.client().execute(() -> {
                if (context.client().player != null && context.client().player.isCreativeLevelTwoOp() && context.client().world != null && context.client().world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    context.client().setScreen(new VillagerQuestOpScreen(merchantEntity, defaultChangeableName, defaultInvincibility, defaultOffersTrades));
                }
            });
        });
    }

    public static void writeC2SScreenPacket(MerchantEntity merchantEntity, int mouseX, int mouseY, boolean villagerScreen) {
        ((MerchantAccessor) merchantEntity).setOffersTrades(true);
        ClientPlayNetworking.send(new ScreenPacket(merchantEntity.getId(), mouseX, mouseY, villagerScreen));
    }

    public static void writeC2SCloseScreenPacket(int merchantEntityId) {
        ClientPlayNetworking.send(new CloseScreenPacket(merchantEntityId));
    }

    public static void writeC2SUpdateMerchantQuestMark(UUID uuid) {
        ClientPlayNetworking.send(new UpdateQuestMarkPacket(uuid));
    }

    public static void writeC2SAcceptQuestPacket(long questId, boolean acceptQuest) {
        ClientPlayNetworking.send(new AcceptQuestPacket(questId, acceptQuest));
    }

    public static void writeC2SCompleteQuestPacket(long questId) {
        ClientPlayNetworking.send(new CompleteQuestPacket(questId));
    }

    public static void writeC2STalkPacket(int merchantEntityId, long questId) {
        ClientPlayNetworking.send(new CompleteTalkQuestPacket(merchantEntityId, questId));
    }

    public static void writeC2SOpMerchantPacket(int merchantEntityId, String merchantName, boolean changeableName, boolean invincibility, boolean hasAi, boolean offersTrades) {
        ClientPlayNetworking.send(new OpMerchantPacket(merchantEntityId, merchantName, changeableName, invincibility, hasAi, offersTrades));
    }


}
