package net.villagerquests.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.villagerquests.VillagerQuestsMain;

public class VillagerQuestsEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(InventoryScreen.class, (screen, consumer) -> {
            if (VillagerQuestsMain.CONFIG.exclusionZone) {
                int i = (screen.width - 276) / 2;
                int j = (screen.height - 166) / 2;
                consumer.accept(new Bounds(i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 20, 20));
            }
        });
    }
}
