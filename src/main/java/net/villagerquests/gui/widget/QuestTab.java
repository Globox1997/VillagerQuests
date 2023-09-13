package net.villagerquests.gui.widget;

import org.jetbrains.annotations.Nullable;

import net.libz.api.InventoryTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.network.QuestClientPacket;

public class QuestTab extends InventoryTab {

    public QuestTab(Text title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(title, texture, preferedPos, screenClasses);
    }

    @Override
    public void onClick(MinecraftClient client) {
        QuestClientPacket.writeC2SScreenPacket(((MerchantAccessor) client.player).getCurrentOfferer(), (int) client.mouse.getX(), (int) client.mouse.getY());
    }

}
