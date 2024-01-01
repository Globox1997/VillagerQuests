package net.villagerquests.init;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.util.Identifier;
import net.villagerquests.ftb.VillagerTalkTask;

public class TaskInit {

    public static TaskType VILLAGER_TALK = TaskTypes.register(new Identifier("ftbquests", "villager_talk"), VillagerTalkTask::new, () -> Icon.getIcon("minecraft:item/diamond_boots"));

    public static void init() {
    }

}
