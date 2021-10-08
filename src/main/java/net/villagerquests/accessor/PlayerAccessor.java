package net.villagerquests.accessor;

import java.util.List;
import java.util.UUID;

public interface PlayerAccessor {

    public void addPlayerQuestId(int id, UUID uuid);

    // Accepted Quests
    public List<Integer> getPlayerQuestIdList();

    // Killed Mob Count
    public List<List<Integer>> getPlayerKilledQuestList();

    // Travel Ids
    public List<List<Object>> getPlayerTravelList();

    // Trader Ids
    public List<UUID> getPlayerQuestTraderIdList();

    // Finished Quests
    public List<Integer> getPlayerFinishedQuestIdList();

    // Timer
    public List<Integer> getPlayerQuestTimerList();

    // Refresh Timer
    public List<Integer> getPlayerQuestRefreshTimerList();

    public boolean isOriginalQuestGiver(UUID uuid, int questId);

    public boolean canAddKilledMobQuestCount(int entityRawId);

    public void finishPlayerQuest(int id);

    public void failPlayerQuest(int id, int reason);

    public void syncPlayerQuest(List<Integer> questIds, List<List<Integer>> killedCount, List<List<Object>> travelIds, List<UUID> traderUuids, List<Integer> finishedIds, List<Integer> timers,
            List<Integer> refresher);

}
