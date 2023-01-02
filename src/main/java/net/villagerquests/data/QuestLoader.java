package net.villagerquests.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.villagerquests.VillagerQuestsMain;

public class QuestLoader implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return new Identifier("villagerquests", "quest_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        QuestLoader.clearEveryList();
        for (Map.Entry<Identifier, Resource> entry : manager.findResources("quests", id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier id = entry.getKey();
            try {
                InputStream stream = entry.getValue().getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                if (QuestData.idList.contains(data.get("id").getAsInt())) {
                    VillagerQuestsMain.LOGGER.error("Error occurred while loading resource {}. Quest with Id: {} got loaded more than one time", id.toString(), data.get("id").getAsInt());
                } else {
                    // Id
                    QuestData.idList.add((validateNumber(data.get("id"))));
                    // Title
                    QuestData.titleList.add(data.get("title").getAsString());
                    // Level
                    if (data.has("level"))
                        QuestData.levelList.add(validateNumber(data.get("level")));
                    else
                        QuestData.levelList.add(0);
                    // Type
                    QuestData.typeList.add(data.get("type").getAsString());
                    // Profession
                    if (!Registry.VILLAGER_PROFESSION.containsId(new Identifier(data.get("profession").getAsString())))
                        VillagerQuestsMain.LOGGER.error("Error occurred while loading quest {}. Profession {} does not exist", data.get("id").getAsInt(), data.get("profession").getAsString());
                    QuestData.professionList.add(data.get("profession").getAsString());
                    // Task
                    ArrayList<Object> taskList = new ArrayList<Object>();
                    for (int i = 0; i < data.getAsJsonArray("task").size(); i++) {
                        if (data.getAsJsonArray("task").get(i).toString().matches("-?(0|[1-9]\\d*)")) {
                            taskList.add(validateNumber(data.getAsJsonArray("task").get(i)));
                        } else {
                            taskList.add(data.getAsJsonArray("task").get(i).getAsString());
                            if (i == 0)
                                continue;
                            String lastTaskString = data.getAsJsonArray("task").get(i - 1).getAsString();
                            if (lastTaskString.equals("kill")) {
                                if (!Registry.ENTITY_TYPE.containsId(new Identifier((String) taskList.get(taskList.size() - 1))))
                                    VillagerQuestsMain.LOGGER.error("Error occurred while loading quest {}. EntityType {} is null", data.get("id").getAsInt(),
                                            data.getAsJsonArray("task").get(i).getAsString());
                            } else if (lastTaskString.equals("travel") || lastTaskString.equals("explore")) {
                                if (!BuiltinRegistries.DYNAMIC_REGISTRY_MANAGER.get(Registry.STRUCTURE_KEY).containsId(new Identifier((String) taskList.get(taskList.size() - 1)))
                                        && !BuiltinRegistries.DYNAMIC_REGISTRY_MANAGER.get(Registry.BIOME_KEY).containsId(new Identifier((String) taskList.get(taskList.size() - 1)))
                                        && VillagerQuestsMain.CONFIG.structureRegistryCheck)
                                    VillagerQuestsMain.LOGGER.error("Error occurred while loading quest {}. Structure or Biome {} is null", data.get("id").getAsInt(),
                                            data.getAsJsonArray("task").get(i).getAsString());
                            } else if (lastTaskString.equals("submit") || lastTaskString.equals("farm") || lastTaskString.equals("mine")) {
                                if (!Registry.ITEM.containsId(new Identifier((String) taskList.get(taskList.size() - 1))))
                                    VillagerQuestsMain.LOGGER.error("Error occurred while loading quest {}. Item {} is null", data.get("id").getAsInt(),
                                            data.getAsJsonArray("task").get(i).getAsString());
                            }
                        }
                    }
                    QuestData.taskList.add(taskList);
                    // Description
                    QuestData.descriptionList.add(data.get("description").getAsString());
                    // Experience
                    if (data.has("experience"))
                        QuestData.experienceList.add(validateNumber(data.get("experience")));
                    else
                        QuestData.experienceList.add(0);
                    // Reward
                    ArrayList<Object> rewardList = new ArrayList<Object>();
                    for (int i = 0; i < data.getAsJsonArray("reward").size(); i++) {
                        if (data.getAsJsonArray("reward").get(i).toString().matches("-?(0|[1-9]\\d*)")) {
                            rewardList.add(validateNumber(data.getAsJsonArray("reward").get(i)));
                        } else {
                            rewardList.add(data.getAsJsonArray("reward").get(i).getAsString());
                            if (!Registry.ITEM.containsId(new Identifier((String) rewardList.get(rewardList.size() - 1))))
                                VillagerQuestsMain.LOGGER.error("Error occurred while loading quest {}. Reward Item {} is null", data.get("id").getAsInt(),
                                        data.getAsJsonArray("reward").get(i).getAsString());
                        }
                    }
                    QuestData.rewardList.add(rewardList);
                    // Refresh
                    if (data.has("refresh"))
                        QuestData.refreshTimeList.add(data.get("refresh").getAsInt());
                    else
                        QuestData.refreshTimeList.add(-1);
                    // Timer
                    if (data.has("timer"))
                        QuestData.timerList.add(data.get("timer").getAsInt());
                    else
                        QuestData.timerList.add(-1);
                }

            } catch (Exception e) {
                VillagerQuestsMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        }

    }

    private static int validateNumber(Number num){
        return Math.max(num.intValue(), 0);
    }

    private static int validateNumber(JsonElement element){
        return validateNumber(element.getAsNumber());
    }

    public static void clearEveryList() {
        QuestData.descriptionList.clear();
        QuestData.experienceList.clear();
        QuestData.idList.clear();
        QuestData.levelList.clear();
        QuestData.professionList.clear();
        QuestData.rewardList.clear();
        QuestData.taskList.clear();
        QuestData.titleList.clear();
        QuestData.typeList.clear();
        QuestData.refreshTimeList.clear();
        QuestData.timerList.clear();
    }

}