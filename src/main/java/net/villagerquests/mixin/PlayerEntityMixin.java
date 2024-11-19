package net.villagerquests.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.villagerquests.access.MerchantAccessor;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements MerchantAccessor {

    @Nullable
    @Unique
    private MerchantEntity tradingEntity = null;

    @Override
    public void setCurrentOfferer(MerchantEntity merchantEntity) {
        this.tradingEntity = merchantEntity;
    }

    @Nullable
    @Override
    public MerchantEntity getCurrentOfferer() {
        return this.tradingEntity;
    }

}
