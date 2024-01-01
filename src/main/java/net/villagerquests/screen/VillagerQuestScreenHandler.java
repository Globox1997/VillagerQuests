package net.villagerquests.screen;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.access.MerchantScreenHandlerAccessor;
import net.villagerquests.init.ScreenInit;

public class VillagerQuestScreenHandler extends ScreenHandler implements MerchantScreenHandlerAccessor {

    public final MerchantEntity offerer;
    private boolean switchingScreen = false;

    public VillagerQuestScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenInit.VILLAGERQUEST_SCREEN_HANDLER_TYPE, syncId);
        this.offerer = ((MerchantAccessor) playerInventory.player).getCurrentOfferer();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.offerer.getCustomer() == player;
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSwitchingScreen() {
        return this.switchingScreen;
    }

    @Override
    public void setSwitchingScreen(boolean switchingScreen) {
        this.switchingScreen = switchingScreen;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!this.switchingScreen) {
            offerer.setCustomer(null);
        }
    }

}