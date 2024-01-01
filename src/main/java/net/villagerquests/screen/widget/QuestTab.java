package net.villagerquests.screen.widget;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.InventoryTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.network.QuestClientPacket;

@Environment(EnvType.CLIENT)
public class QuestTab extends InventoryTab {

    public QuestTab(Text title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(title, texture, preferedPos, screenClasses);
    }

    @Override
    public boolean shouldShow(MinecraftClient client) {
        if (!((MerchantAccessor) ((MerchantAccessor) client.player).getCurrentOfferer()).getOffersTrades()) {
            return false;
        }
        return super.shouldShow(client);
    }

    @Override
    public void onClick(MinecraftClient client) {
        QuestClientPacket.writeC2SScreenPacket(((MerchantAccessor) client.player).getCurrentOfferer(), (int) client.mouse.getX(), (int) client.mouse.getY(), true);
    }

}
