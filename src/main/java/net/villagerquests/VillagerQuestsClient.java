package net.villagerquests;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.villagerquests.gui.QuestScreen;
import net.villagerquests.gui.QuestScreenHandler;
import net.villagerquests.network.QuestClientPacket;

public class VillagerQuestsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.<QuestScreenHandler, QuestScreen>register(VillagerQuestsMain.QUEST_SCREEN_HANDLER_TYPE, (gui, inventory, title) -> new QuestScreen(gui, inventory.player, title));
        QuestClientPacket.init();
    }

}
