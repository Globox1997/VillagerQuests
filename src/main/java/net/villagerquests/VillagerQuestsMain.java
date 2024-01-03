package net.villagerquests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.villagerquests.init.ConfigInit;
import net.villagerquests.init.EventInit;
import net.villagerquests.init.ScreenInit;
import net.villagerquests.init.TaskInit;
import net.villagerquests.network.QuestServerPacket;

public class VillagerQuestsMain implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("VillagerQuests");

    // Todo:
    // - Op Screen Quest Selection
    // - Timer on Quests

    @Override
    public void onInitialize() {
        ConfigInit.init();
        TaskInit.init();
        ScreenInit.init();
        EventInit.init();
        QuestServerPacket.init();
    }

}
