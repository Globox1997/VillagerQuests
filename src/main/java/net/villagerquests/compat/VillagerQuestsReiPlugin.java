package net.villagerquests.compat;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.villagerquests.VillagerQuestsMain;

import java.util.Collections;

public class VillagerQuestsReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        if (VillagerQuestsMain.CONFIG.reiExclusionzone)
            zones.register(MerchantScreen.class, screen -> {
                int i = (screen.width - 276) / 2;
                int j = (screen.height - 166) / 2;
                return Collections.singleton(new Rectangle(i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 20, 20));
            });
    }
}
