package net.villagerquests.init;

import java.util.Iterator;
import java.util.List;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.screen.VillagerQuestScreenHandler;
import net.villagerquests.util.QuestHelper;

public class EventInit {

    public static void init() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof MerchantEntity merchantEntity) {
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
            VillagerQuestState.getPlayerVillagerQuestState(playerLoggedInAfterTeamEvent.getPlayer()).getMerchantQuestMarkMap().forEach((uuid, questMarkType) -> {
                if (questMarkType == -1) {
                    questMarkType = QuestHelper.getVillagerQuestMarkType(playerLoggedInAfterTeamEvent.getPlayer(), uuid);
                }
            });
        });
    }

}
