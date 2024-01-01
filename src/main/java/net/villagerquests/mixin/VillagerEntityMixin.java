package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.passive.VillagerEntity;
import net.villagerquests.access.MerchantAccessor;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

    @Redirect(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;resetCustomer()V"))
    private void mobTickMixin(VillagerEntity villagerEntity) {
        if (((MerchantAccessor) villagerEntity).getOffersTrades()) {
            resetCustomer();
        }
    }

    @Shadow
    protected void resetCustomer() {
    }
}
