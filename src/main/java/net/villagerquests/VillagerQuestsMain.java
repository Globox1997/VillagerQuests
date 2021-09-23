package net.villagerquests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.villagerquests.config.VillagerQuestsConfig;
import net.villagerquests.data.QuestLoader;
import net.villagerquests.gui.QuestScreenHandler;
import net.villagerquests.network.QuestServerPacket;

public class VillagerQuestsMain implements ModInitializer {
    public static VillagerQuestsConfig CONFIG = new VillagerQuestsConfig();
    public static ScreenHandlerType<QuestScreenHandler> QUEST_SCREEN_HANDLER_TYPE;
    public static final Logger LOGGER = LogManager.getLogger("VillagerQuests");

    @Override
    public void onInitialize() {
        QUEST_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(new Identifier("villagerquests", "quest_screen_type"),
                (syncId, inventory) -> new QuestScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY, new MerchantScreenHandler(syncId, inventory)));
        AutoConfig.register(VillagerQuestsConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VillagerQuestsConfig.class).getConfig();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new QuestLoader());
        QuestServerPacket.init();
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                    QuestServerPacket.writeS2CQuestListPacket(server.getPlayerManager().getPlayerList().get(i));
                }
                LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else {
                LOGGER.error("Failed to reload on {}", Thread.currentThread());
            }
        });
    }

}
