package net.villagerquests;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.libz.registry.TabRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.gui.QuestScreen;
import net.villagerquests.gui.widget.QuestTab;
import net.villagerquests.gui.widget.TradeTab;
import net.villagerquests.network.QuestClientPacket;

public class VillagerQuestsClient implements ClientModInitializer {

    public static final EntityModelLayer QUEST_LAYER = new EntityModelLayer(new Identifier("villagerquests:quest_layer"), "quest_layer");
    public static KeyBinding questKey;

    private static final Identifier TRADE_TAB_ICON = new Identifier("villagerquests:textures/gui/trade_tab_icon.png");
    private static final Identifier QUEST_TAB_ICON = new Identifier("villagerquests:textures/gui/quest_tab_icon.png");

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
                // client.setScreen(screen);
                // client.setScreen(new PlayerQuestScreen(new PlayerQuestGui(client)));
                return;
            }

        });

        HandledScreens.register(VillagerQuestsMain.QUEST_SCREEN_HANDLER_TYPE, QuestScreen::new);
        QuestClientPacket.init();
        EntityModelLayerRegistry.registerModelLayer(QUEST_LAYER, QuestEntityModel::getTexturedModelData);

        TabRegistry.registerOtherTab(new TradeTab(Text.translatable("merchant.trades"), TRADE_TAB_ICON, 0, MerchantScreen.class), MerchantScreen.class);
        TabRegistry.registerOtherTab(new QuestTab(Text.translatable("screen.villagerquests"), QUEST_TAB_ICON, 1, QuestScreen.class), MerchantScreen.class);
    }

}
