package net.villagerquests.init;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.net.ObjectCompletedResetMessage;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.access.TeamDataAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.screen.VillagerQuestScreenHandler;
import net.villagerquests.util.QuestHelper;

public class EventInit {

    public static void init() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof MerchantEntity merchantEntity && !merchantEntity.hasCustomer()) {
                if (!PlayerHooks.isFake(player)) {
                    List<VillagerTalkTask> talkTasks = ServerQuestFile.INSTANCE.collect(VillagerTalkTask.class);
                    if (!talkTasks.isEmpty()) {
                        TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
                        Iterator<VillagerTalkTask> iterator = talkTasks.iterator();

                        while (iterator.hasNext()) {
                            VillagerTalkTask task = iterator.next();
                            if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.getQuest()) && task.getVillagerUuid().equals(merchantEntity.getUuid())) {
                                QuestServerPacket.writeS2CTalkPacket((ServerPlayerEntity) player, entity.getId(), task.getQuest().id);
                                return ActionResult.CONSUME;
                            }
                        }
                    }
                }
                if (player.isCreativeLevelTwoOp() && player.isSneaking() && player.getMainHandStack().isEmpty()) {
                    QuestServerPacket.writeS2COpMerchantScreenPacket((ServerPlayerEntity) player, merchantEntity);
                    return ActionResult.CONSUME;
                } else if (!((MerchantAccessor) merchantEntity).getOffersTrades()) {
                    if (!(player.currentScreenHandler instanceof VillagerQuestScreenHandler)) {
                        merchantEntity.setCustomer(player);
                        QuestServerPacket.writeS2COffersTradesPacket((ServerPlayerEntity) player, merchantEntity.getId(), false);
                        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
                            return new VillagerQuestScreenHandler(syncId, playerInventory);
                        }, Text.of("")));
                        return ActionResult.CONSUME;
                    }
                } else {
                    QuestServerPacket.writeS2COffersTradesPacket((ServerPlayerEntity) player, merchantEntity.getId(), true);
                }
            }
            return ActionResult.PASS;
        });

        TeamEvent.PLAYER_LOGGED_IN.register((playerLoggedInAfterTeamEvent) -> {
            Iterator<Map.Entry<UUID, Integer>> iterator = VillagerQuestState
                    .getPlayerVillagerQuestState(playerLoggedInAfterTeamEvent.getPlayer().getServer(), playerLoggedInAfterTeamEvent.getPlayer().getUuid()).getMerchantQuestMarkMap().entrySet()
                    .iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                if (entry.getValue() == -1) {
                    VillagerQuestState.updatePlayerVillagerQuestMarkType(playerLoggedInAfterTeamEvent.getPlayer().getServer(), playerLoggedInAfterTeamEvent.getPlayer().getUuid(), entry.getKey(),
                            QuestHelper.getVillagerQuestMarkType(playerLoggedInAfterTeamEvent.getPlayer(), entry.getKey()));
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            if (server.getOverworld().getTime() % 20 == 0) {
                Iterator<TeamData> iterator = FTBQuestsAPI.api().getQuestFile(false).getAllTeamData().iterator();
                while (iterator.hasNext()) {
                    TeamData teamData = iterator.next();
                    if (((TeamDataAccessor) teamData).getTimer().size() > 0) {
                        List<Long> removalList = null;
                        Iterator<Map.Entry<Long, Long>> timerIterator = ((TeamDataAccessor) teamData).getTimer().entrySet().iterator();
                        while (timerIterator.hasNext()) {
                            Map.Entry<Long, Long> entry = timerIterator.next();
                            if (teamData.getFile().getQuest(entry.getKey()) != null && !((TeamDataAccessor) teamData).getCompleted().containsKey((long) entry.getKey())) {
                                int timer = ((QuestAccessor) (Object) teamData.getFile().getQuest(entry.getKey())).getTimer();
                                if (server.getOverworld().getTime() > entry.getValue() + (long) timer) {
                                    ((TeamDataAccessor) teamData).setQuestStarted(entry.getKey(), null);
                                    teamData.getFile().getQuest(entry.getKey()).getTasks().forEach(task -> {
                                        teamData.resetProgress(task);
                                    });
                                    teamData.clearCachedProgress();
                                    teamData.markDirty();
                                    new ObjectCompletedResetMessage(teamData.getTeamId(), entry.getKey()).sendTo(teamData.getOnlineMembers());
                                    if (((QuestAccessor) (Object) teamData.getFile().getQuest(entry.getKey())).isVillagerQuest()) {
                                        QuestHelper.updateTeamQuestMark(server, teamData, ((QuestAccessor) (Object) teamData.getFile().getQuest(entry.getKey())).getVillagerQuestUuid());
                                    }
                                    removalList = new ArrayList<Long>();
                                    removalList.add(entry.getKey());

                                    List<ServerPlayerEntity> list = teamData.getOnlineMembers().stream().toList();
                                    for (int i = 0; i < list.size(); i++) {
                                        QuestServerPacket.writeS2CFailQuestPacket(list.get(i), entry.getKey());
                                    }
                                }

                            }
                        }
                        if (removalList != null && !removalList.isEmpty()) {
                            for (int i = 0; i < removalList.size(); i++) {
                                ((TeamDataAccessor) teamData).getTimer().remove(removalList.get(i));
                            }
                        }
                        // ((TeamDataAccessor) teamData).getTimer().forEach((questId, acceptedTime) -> {
                        // if (teamData.getFile().getQuest(questId) != null && !((TeamDataAccessor) teamData).getCompleted().containsKey((long) questId)) {
                        // int timer = ((QuestAccessor) (Object) teamData.getFile().getQuest(questId)).getTimer();
                        // if (server.getOverworld().getTime() > acceptedTime + (long) timer) {
                        // ((TeamDataAccessor) teamData).setQuestStarted(questId, null);
                        // teamData.getFile().getQuest(questId).getTasks().forEach(task -> {
                        // teamData.resetProgress(task);
                        // });
                        // teamData.clearCachedProgress();
                        // teamData.markDirty();
                        // new ObjectCompletedResetMessage(teamData.getTeamId(), questId).sendTo(teamData.getOnlineMembers());
                        // if (((QuestAccessor) (Object) teamData.getFile().getQuest(questId)).isVillagerQuest()) {
                        // QuestHelper.updateTeamQuestMark(server, teamData, ((QuestAccessor) (Object) teamData.getFile().getQuest(questId)).getVillagerQuestUuid());
                        // }
                        // }
                        // }
                        // });
                    }
                }
            }
        });
    }

}
