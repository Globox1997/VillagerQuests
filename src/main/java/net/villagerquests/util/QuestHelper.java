package net.villagerquests.util;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.access.TeamDataAccessor;

public class QuestHelper {

    /*
     * 0 = None 1 = Question Mark 2 = Exclamation Mark
     */
    public static int getVillagerQuestMarkType(ServerPlayerEntity serverPlayerEntity, UUID uuid) {
        int questMarkType = 0;
        if (!serverPlayerEntity.getWorld().isClient()) {
            TeamData teamData = ServerQuestFile.INSTANCE.getOrCreateTeamData(serverPlayerEntity);
            if (teamData == null) {
                return -1;
            }
            boolean hasOpenQuest = false;
            for (int i = 0; i < ServerQuestFile.INSTANCE.getAllChapters().size(); i++) {
                List<Quest> quests = ServerQuestFile.INSTANCE.getAllChapters().get(i).getQuests();
                for (int u = 0; u < quests.size(); u++) {
                    Quest quest = quests.get(u);
                    if (((QuestAccessor) (Object) quest).isVillagerQuest() && ((QuestAccessor) (Object) quest).getVillagerQuestUuid().equals(uuid)
                            && ((QuestAccessor) (Object) quest).isQuestVisible(teamData) && !teamData.isCompleted(quest)) {
                        questMarkType = 1;
                        if (quest.isCompletedRaw(teamData)) {
                            Iterator<Reward> iterator = quest.getRewards().iterator();
                            while (iterator.hasNext()) {
                                if (!teamData.isRewardClaimed(serverPlayerEntity.getUuid(), iterator.next())) {
                                    questMarkType = 2;
                                    break;
                                }
                            }
                            break;
                        } else if (teamData.isStarted(quest)) {
                            questMarkType = 0;
                        } else {
                            hasOpenQuest = true;
                        }
                    }
                }
                if (questMarkType == 2) {
                    break;
                }
            }
            if (hasOpenQuest && questMarkType == 0) {
                questMarkType = 1;
            }
        }
        return questMarkType;
    }

    public static void removeVillagerQuestFromQuest(UUID uuid) {
        for (int i = 0; i < ServerQuestFile.INSTANCE.getAllChapters().size(); i++) {
            List<Quest> quests = ServerQuestFile.INSTANCE.getAllChapters().get(i).getQuests();
            for (int u = 0; u < quests.size(); u++) {
                Quest quest = quests.get(u);
                if (((QuestAccessor) (Object) quest).isVillagerQuest() && ((QuestAccessor) (Object) quest).getVillagerQuestUuid().equals(uuid)) {

                    ((QuestAccessor) (Object) quest).setVillagerQuest(false);
                    ((QuestAccessor) (Object) quest).setVillagerQuestUuid(null);

                    Iterator<TeamData> iterator = FTBQuestsAPI.api().getQuestFile(false).getAllTeamData().iterator();
                    while (iterator.hasNext()) {
                        ((TeamDataAccessor) iterator.next()).setQuestStarted(quest.id, null);
                    }
                }
            }
        }
    }

}
