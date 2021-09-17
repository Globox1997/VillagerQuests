package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.villagerquests.accessor.MerchantAccessor;

@Mixin(MerchantScreenHandler.class)
public abstract class MerchantScreenHandlerMixin extends ScreenHandler implements MerchantAccessor {

    private final PlayerInventory playerInventory;
    @Shadow
    @Final
    private Merchant merchant;

    public MerchantScreenHandlerMixin(int syncId, PlayerInventory playerInventory, Merchant merchant) {
        super(ScreenHandlerType.MERCHANT, syncId);
        this.playerInventory = playerInventory;
    }

    // @Inject(method = "close", at = @At(value = "TAIL"))
    // private void closeMixin(PlayerEntity player, CallbackInfo info) {
    // if (player != null)
    // System.out.println("CLOSINGGGGG " + player.currentScreenHandler + ":" + merchant.getCurrentCustomer());
    // }

    // @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/Merchant;setCurrentCustomer(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    // private void closeRedirectMixin(Merchant merchant, PlayerEntity nullPlayer, PlayerEntity player) {
    // if (player != null)
    // System.out.println("Redirect: " + player.currentScreenHandler + " " + merchant.getCurrentCustomer());
    // System.out.println("Redirect: " + merchant.getCurrentCustomer());
    // // if (playerInventory.player != null)
    // // System.out.println("KJ");
    // }
    // Lnet/minecraft/village/Merchant;setCurrentCustomer(Lnet/minecraft/entity/player/PlayerEntity;)V

}
