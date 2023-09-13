package net.villagerquests.gui.widget;

import org.jetbrains.annotations.Nullable;

import net.libz.api.InventoryTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.gui.QuestScreen;
import net.villagerquests.network.QuestClientPacket;

public class TradeTab extends InventoryTab {

    public TradeTab(Text title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(title, texture, preferedPos, screenClasses);
    }

    @Override
    public void onClick(MinecraftClient client) {
        ((QuestScreen) client.currentScreen).setClosedToTradeScreen();
        QuestClientPacket.writeC2STradePacket(((MerchantAccessor) client.player).getCurrentOfferer(), (int) client.mouse.getX(), (int) client.mouse.getY());
    }

}
