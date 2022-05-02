package net.villagerquests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import net.villagerquests.command.QuestCommands;
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
        QUEST_SCREEN_HANDLER_TYPE = Registry.register(Registry.SCREEN_HANDLER, "villagerquests",
                new ScreenHandlerType<>((syncId, inventory) -> new QuestScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY, new MerchantScreenHandler(syncId, inventory))));
        AutoConfig.register(VillagerQuestsConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VillagerQuestsConfig.class).getConfig();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new QuestLoader());
        QuestServerPacket.init();
        QuestCommands.init();
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                    QuestServerPacket.writeS2CQuestListPacket(server.getPlayerManager().getPlayerList().get(i));
                    QuestServerPacket.writeS2CPlayerQuestDataPacket(server.getPlayerManager().getPlayerList().get(i));
                }
                LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else {
                LOGGER.error("Failed to reload on {}", Thread.currentThread());
            }
        });

        /*
         * Biomes: [minecraft:ocean, minecraft:plains, minecraft:desert, minecraft:mountains, minecraft:forest, minecraft:taiga, minecraft:swamp, minecraft:river, minecraft:nether_wastes,
         * minecraft:the_end, minecraft:frozen_ocean, minecraft:frozen_river, minecraft:snowy_tundra, minecraft:snowy_mountains, minecraft:mushroom_fields, minecraft:mushroom_field_shore,
         * minecraft:beach, minecraft:desert_hills, minecraft:wooded_hills, minecraft:taiga_hills, minecraft:mountain_edge, minecraft:jungle, minecraft:jungle_hills, minecraft:jungle_edge,
         * minecraft:deep_ocean, minecraft:stone_shore, minecraft:snowy_beach, minecraft:birch_forest, minecraft:birch_forest_hills, minecraft:dark_forest, minecraft:snowy_taiga,
         * minecraft:snowy_taiga_hills, minecraft:giant_tree_taiga, minecraft:giant_tree_taiga_hills, minecraft:wooded_mountains, minecraft:savanna, minecraft:savanna_plateau, minecraft:badlands,
         * minecraft:wooded_badlands_plateau, minecraft:badlands_plateau, minecraft:small_end_islands, minecraft:end_midlands, minecraft:end_highlands, minecraft:end_barrens, minecraft:warm_ocean,
         * minecraft:lukewarm_ocean, minecraft:cold_ocean, minecraft:deep_warm_ocean, minecraft:deep_lukewarm_ocean, minecraft:deep_cold_ocean, minecraft:deep_frozen_ocean, minecraft:the_void,
         * minecraft:sunflower_plains, minecraft:desert_lakes, minecraft:gravelly_mountains, minecraft:flower_forest, minecraft:taiga_mountains, minecraft:swamp_hills, minecraft:ice_spikes,
         * minecraft:modified_jungle, minecraft:modified_jungle_edge, minecraft:tall_birch_forest, minecraft:tall_birch_hills, minecraft:dark_forest_hills, minecraft:snowy_taiga_mountains,
         * minecraft:giant_spruce_taiga, minecraft:giant_spruce_taiga_hills, minecraft:modified_gravelly_mountains, minecraft:shattered_savanna, minecraft:shattered_savanna_plateau,
         * minecraft:eroded_badlands, minecraft:modified_wooded_badlands_plateau, minecraft:modified_badlands_plateau, minecraft:bamboo_jungle, minecraft:bamboo_jungle_hills,
         * minecraft:soul_sand_valley, minecraft:crimson_forest, minecraft:warped_forest, minecraft:basalt_deltas, minecraft:dripstone_caves, minecraft:lush_caves] Structures
         * [minecraft:pillager_outpost, minecraft:mineshaft, minecraft:mansion, minecraft:jungle_pyramid, minecraft:desert_pyramid, minecraft:igloo, minecraft:ruined_portal, minecraft:shipwreck,
         * minecraft:swamp_hut, minecraft:stronghold, minecraft:monument, minecraft:ocean_ruin, minecraft:fortress, minecraft:endcity, minecraft:buried_treasure, minecraft:village,
         * minecraft:nether_fossil, minecraft:bastion_remnant]
         */
    }

}
