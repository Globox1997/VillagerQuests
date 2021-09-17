package net.villagerquests.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class QuestLoader implements SimpleSynchronousResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger("VillagerQuests");

    @Override
    public Identifier getFabricId() {
        return new Identifier("villagerquests", "quest_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        this.clearEveryList();
        for (Identifier id : manager.findResources("quests", path -> path.endsWith(".json"))) {
            try {
                InputStream stream = manager.getResource(id).getInputStream();
                JsonObject data = new JsonParser().parse(new InputStreamReader(stream)).getAsJsonObject();
                if (QuestData.idList.contains(data.get("id").getAsInt())) {
                    LOGGER.error("Error occurred while loading resource {}. Quest with Id: {} got loaded more than one time", id.toString(), data.get("id").getAsInt());
                }
                // Id
                QuestData.idList.add(data.get("id").getAsInt());
                // Title
                QuestData.titleList.add(data.get("title").getAsString());
                // Level
                QuestData.levelList.add(data.get("level").getAsInt());
                // Type
                QuestData.typeList.add(data.get("type").getAsString());
                // Profession
                QuestData.professionList.add(data.get("profession").getAsString());
                // Task
                ArrayList<Object> taskList = new ArrayList<Object>();
                for (int i = 0; i < data.getAsJsonArray("task").size(); i++) {
                    if (data.getAsJsonArray("task").get(i).toString().matches("-?(0|[1-9]\\d*)")) {
                        taskList.add(data.getAsJsonArray("task").get(i).getAsInt());
                    } else
                        taskList.add(data.getAsJsonArray("task").get(i).getAsString());
                }
                QuestData.taskList.add(taskList);
                // Description
                QuestData.descriptionList.add(data.get("description").getAsString());
                // Experience
                QuestData.experienceList.add(data.get("experience").getAsInt());
                // Reward
                ArrayList<Object> rewardList = new ArrayList<Object>();
                for (int i = 0; i < data.getAsJsonArray("reward").size(); i++) {
                    if (data.getAsJsonArray("reward").get(i).toString().matches("-?(0|[1-9]\\d*)")) {
                        rewardList.add(data.getAsJsonArray("reward").get(i).getAsInt());
                    } else
                        rewardList.add(data.getAsJsonArray("reward").get(i).getAsString());
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

            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        }

    }

    private void clearEveryList() {
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