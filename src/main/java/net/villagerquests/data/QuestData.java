package net.villagerquests.data;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class QuestData {
    // Id
    public static final List<Integer> idList = new ArrayList<>();
    // Title
    public static final List<String> titleList = new ArrayList<>();
    // Level
    public static final List<Integer> levelList = new ArrayList<>();
    // Type
    public static final List<String> typeList = new ArrayList<>();
    // Profession
    public static final List<String> professionList = new ArrayList<>();
    // Task
    public static final List<List<Object>> taskList = new ArrayList<>();
    // Description
    public static final List<String> descriptionList = new ArrayList<>();
    // Experience
    public static final List<Integer> experienceList = new ArrayList<>();
    // Reward
    public static final List<List<Object>> rewardList = new ArrayList<>();
    // Refresh
    public static final List<Integer> refreshTimeList = new ArrayList<>();
    // Timer
    public static final List<Integer> timerList = new ArrayList<>();

    @Nullable
    public static List getList(String string) {
        switch (string) {
            case "questIdList":
                return idList;
            case "questTitleList":
                return titleList;
            case "questLevelList":
                return levelList;
            case "questTypeList":
                return typeList;
            case "questProfessionList":
                return professionList;
            case "questTaskList":
                return taskList;
            case "questDecriptionList":
                return descriptionList;
            case "questExperienceList":
                return experienceList;
            case "questRewardList":
                return rewardList;
            case "questRefreshTimeList":
                return refreshTimeList;
            case "questTimerList":
                return timerList;
            default:
                return null;
        }
    }

    public static ArrayList<String> getListNames() {
        ArrayList<String> listNames = new ArrayList<String>();
        listNames.add("questIdList");
        listNames.add("questTitleList");
        listNames.add("questLevelList");
        listNames.add("questTypeList");
        listNames.add("questProfessionList");
        listNames.add("questTaskList");
        listNames.add("questDecriptionList");
        listNames.add("questExperienceList");
        listNames.add("questRewardList");
        listNames.add("questRefreshTimeList");
        listNames.add("questTimerList");
        return listNames;
    }

}
