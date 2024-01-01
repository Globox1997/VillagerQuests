package net.villagerquests.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.villagerquests.screen.VillagerQuestScreenHandler;

public class ScreenInit {

    public static final ScreenHandlerType<VillagerQuestScreenHandler> VILLAGERQUEST_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(VillagerQuestScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static void init() {
        Registry.register(Registries.SCREEN_HANDLER, "villagerquests:villagerquest_screen_handler", VILLAGERQUEST_SCREEN_HANDLER_TYPE);
    }

}
