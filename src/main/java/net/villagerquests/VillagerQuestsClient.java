package net.villagerquests;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.gui.QuestScreen;
import net.villagerquests.gui.QuestScreenHandler;
import net.villagerquests.network.QuestClientPacket;

public class VillagerQuestsClient implements ClientModInitializer {

    public static final EntityModelLayer QUEST_LAYER = new EntityModelLayer(new Identifier("villagerquests:quest_layer"), "quest_layer");

    @Override
    public void onInitializeClient() {
        ScreenRegistry.<QuestScreenHandler, QuestScreen>register(VillagerQuestsMain.QUEST_SCREEN_HANDLER_TYPE, (gui, inventory, title) -> new QuestScreen(gui, inventory.player, title));
        QuestClientPacket.init();
        EntityModelLayerRegistry.registerModelLayer(QUEST_LAYER, QuestEntityModel::getTexturedModelData);
    }

}
