package net.villagerquests.util;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;

public class QuestHelper {

    public static int getVillagerQuestMarkType(ServerPlayerEntity serverPlayerEntity, UUID uuid) {
        int questMarkType = 0;
        if (!serverPlayerEntity.getWorld().isClient()) {
            TeamData teamData = ServerQuestFile.INSTANCE.getOrCreateTeamData(serverPlayerEntity);
            if (teamData == null) {
                return -1;
            }
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
                        } else if (((QuestAccessor) (Object) quest).isAccepted()) { // Not sure here
                            questMarkType = 0;
                        }
                    }
                }
                if (questMarkType == 2) {
                    break;
                }
            }
        }
        return questMarkType;
    }

}
