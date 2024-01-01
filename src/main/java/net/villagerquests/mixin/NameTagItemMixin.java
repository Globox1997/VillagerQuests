package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.villagerquests.access.MerchantAccessor;

@Mixin(NameTagItem.class)
public class NameTagItemMixin {

    @Inject(method = "useOnEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setCustomName(Lnet/minecraft/text/Text;)V"), cancellable = true)
    private void useOnEntityMixin(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if (entity instanceof MerchantEntity merchantEntity && !((MerchantAccessor) merchantEntity).getChangeableName()) {
            info.setReturnValue(ActionResult.FAIL);
        }
    }
}
