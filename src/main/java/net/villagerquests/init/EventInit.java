package net.villagerquests.init;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.net.ObjectCompletedResetMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
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

import java.util.*;

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
                            if (data.getProgress(task) < task.getMaxProgress() && data.canStartTasks(task.getQuest()) && task.getVillagerUuid() != null
                                    && task.getVillagerUuid().equals(merchantEntity.getUuid())) {
                                QuestServerPacket.writeS2CTalkPacket((ServerPlayerEntity) player, entity.getId(), task.getQuest().id);
                                merchantEntity.setCustomer(player);
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
            long overWorldTime = server.getOverworld().getTime();
            if (overWorldTime % 20 == 0) {
                for (TeamData rawTeamData : FTBQuestsAPI.api().getQuestFile(false).getAllTeamData()) {
                    TeamDataAccessor teamData = ((TeamDataAccessor) rawTeamData);
                    HashMap<Long, Long> teamDataTimer = teamData.getTimer();
                    if (teamDataTimer.isEmpty()) continue;
                    Iterator<Map.Entry<Long, Long>> timerIterator = teamDataTimer.entrySet().iterator();
                    while (timerIterator.hasNext()) {
                        var entry = timerIterator.next();
                        Quest rawQuest = rawTeamData.getFile().getQuest(entry.getKey());
                        if (rawQuest == null) continue;
                        if (teamData.getCompleted().containsKey((long) entry.getKey())) continue;
                        QuestAccessor questAccessor = ((QuestAccessor) (Object) rawQuest);

                        if (questAccessor == null) continue;
                        int questDataTimer = questAccessor.getTimer();
                        if (questDataTimer <= 0) {
                            continue;
                        }
                        if (overWorldTime > entry.getValue() + (long) questDataTimer) {
                            teamData.setQuestStarted(entry.getKey(), null);
                            rawQuest.getTasks().forEach(rawTeamData::resetProgress);
                            rawTeamData.clearCachedProgress();
                            rawTeamData.markDirty();
                            NetworkManager.sendToPlayers(rawTeamData.getOnlineMembers(), new ObjectCompletedResetMessage(rawTeamData.getTeamId(), entry.getKey()));
                            if (questAccessor.isVillagerQuest()) {
                                QuestHelper.updateTeamQuestMark(server, rawTeamData, questAccessor.getVillagerQuestUuid());
                            }

                            List<ServerPlayerEntity> list = rawTeamData.getOnlineMembers().stream().toList();
                            for (ServerPlayerEntity serverPlayer : list) {
                                QuestServerPacket.writeS2CFailQuestPacket(serverPlayer, entry.getKey());
                            }

                            timerIterator.remove();
                        }
                    }
                }
            }
        });
    }
}
