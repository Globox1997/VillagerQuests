package net.villagerquests.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.libz.registry.TabRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.screen.VillagerQuestScreen;
import net.villagerquests.screen.widget.QuestTab;
import net.villagerquests.screen.widget.TradeTab;

@Environment(EnvType.CLIENT)
public class RenderInit {

    private static final Identifier TRADE_TAB_ICON = new Identifier("villagerquests:textures/gui/trade_tab_icon.png");
    private static final Identifier QUEST_TAB_ICON = new Identifier("villagerquests:textures/gui/quest_tab_icon.png");
    public static final Identifier VILLAGERQUEST_SCREEN_AND_ICONS = new Identifier("villagerquests:textures/gui/screen_and_icons.png");

    public static final EntityModelLayer QUEST_LAYER = new EntityModelLayer(new Identifier("villagerquests:quest_layer"), "quest_layer");

    public static void init() {
        HandledScreens.register(ScreenInit.VILLAGERQUEST_SCREEN_HANDLER_TYPE, VillagerQuestScreen::new);

        TabRegistry.registerOtherTab(new TradeTab(Text.translatable("merchant.trades"), TRADE_TAB_ICON, 0, MerchantScreen.class), MerchantScreen.class);
        TabRegistry.registerOtherTab(new QuestTab(Text.translatable("screen.villagerquests"), QUEST_TAB_ICON, 1, VillagerQuestScreen.class), MerchantScreen.class);

        EntityModelLayerRegistry.registerModelLayer(QUEST_LAYER, QuestEntityModel::getTexturedModelData);

        ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("villagerquests", "villagerquest_theme"), FabricLoader.getInstance().getModContainer("villagerquests").orElseThrow(),
                ResourcePackActivationType.DEFAULT_ENABLED);
    }
}
