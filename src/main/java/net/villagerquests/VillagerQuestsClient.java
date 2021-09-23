package net.villagerquests;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.gui.PlayerQuestGui;
import net.villagerquests.gui.PlayerQuestScreen;
import net.villagerquests.gui.QuestScreen;
import net.villagerquests.gui.QuestScreenHandler;
import net.villagerquests.network.QuestClientPacket;

public class VillagerQuestsClient implements ClientModInitializer {

    public static final EntityModelLayer QUEST_LAYER = new EntityModelLayer(new Identifier("villagerquests:quest_layer"), "quest_layer");
    public static KeyBinding questKey;
    private static boolean questKeyBoolean;

    @Override
    public void onInitializeClient() {

        // InputUtil.UNKNOWN_KEY.getCode()
        // Keybinds
        questKey = new KeyBinding("key.villagerquests.openquestscreen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.villagerquests.keybind");
        // Registering
        KeyBindingHelper.registerKeyBinding(questKey);
        // Callback
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (questKey.wasPressed()) {
                if (!questKeyBoolean) {
                    client.setScreen(new PlayerQuestScreen(new PlayerQuestGui(client)));
                }
                questKeyBoolean = true;
            } else if (questKeyBoolean)
                questKeyBoolean = false;
        });

        ScreenRegistry.<QuestScreenHandler, QuestScreen>register(VillagerQuestsMain.QUEST_SCREEN_HANDLER_TYPE, (gui, inventory, title) -> new QuestScreen(gui, inventory.player, title));
        QuestClientPacket.init();
        EntityModelLayerRegistry.registerModelLayer(QUEST_LAYER, QuestEntityModel::getTexturedModelData);
    }

}
