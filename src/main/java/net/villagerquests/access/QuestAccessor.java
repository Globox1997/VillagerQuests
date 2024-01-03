package net.villagerquests.access;

import java.util.UUID;

import dev.ftb.mods.ftbquests.quest.TeamData;

public interface QuestAccessor {

    public boolean isVillagerQuest();

    public void setVillagerQuest(boolean villagerQuest);

    public UUID getVillagerQuestUuid();

    public void setVillagerQuestUuid(UUID uuid);

    public boolean isQuestVisible(TeamData data);

}
