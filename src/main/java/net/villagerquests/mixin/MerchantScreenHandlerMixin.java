package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import net.villagerquests.access.MerchantScreenHandlerAccessor;

@Mixin(MerchantScreenHandler.class)
public abstract class MerchantScreenHandlerMixin implements MerchantScreenHandlerAccessor {

    private boolean switchingScreen = false;

    @Redirect(method = "onClosed", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/Merchant;setCustomer(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void switchingScreen(Merchant merchant, PlayerEntity playerEntity) {
        if (!switchingScreen) {
            merchant.setCustomer(null);
        }
    }

    @Override
    public void setSwitchingScreen(boolean switchingScreen) {
        this.switchingScreen = switchingScreen;
    }

    @Override
    public boolean isSwitchingScreen() {
        return this.switchingScreen;
    }

}
