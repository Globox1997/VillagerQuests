package net.villagerquests;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
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

    @Override
    public void onInitialize() {

        // QUEST_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(new Identifier("villagerquests", "quest_screen_type"),
        // (syncId, inventory) -> new QuestScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY, null));

        // Screenhandlercontext can be erased?
        QUEST_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(new Identifier("villagerquests", "quest_screen_type"),
                (syncId, inventory) -> new QuestScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY, new MerchantScreenHandler(syncId, inventory)));
        // int syncId, PlayerInventory inventory, PacketByteBuf buf
        // QUEST_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(new Identifier("villagerquests", "quest_screen_type"),
        // (syncId, inventory,buf) -> new QuestScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY, new MerchantScreenHandler(syncId, inventory)));

        // (MerchantScreenHandler) inventory.player.currentScreenHandler
        // COPPER_CHEST = ScreenHandlerRegistry.registerSimple(new Identifier(IronChests.MOD_ID, "copper_chest"),
        // (syncId, inventory) -> new ChestScreenHandler(COPPER_CHEST, ChestTypes.COPPER, syncId, inventory, ScreenHandlerContext.EMPTY));
        // System.out.println(QUEST_SCREEN_HANDLER_TYPE);

        AutoConfig.register(VillagerQuestsConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VillagerQuestsConfig.class).getConfig();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new QuestLoader());
        QuestServerPacket.init();
    }

}
