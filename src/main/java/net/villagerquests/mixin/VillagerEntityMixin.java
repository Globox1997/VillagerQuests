package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestServerPacket;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    private final MerchantEntity merchantEntity = (MerchantEntity) (Object) this;

    @Inject(method = "beginTradeWith", at = @At(value = "HEAD"))
    private void beginTradeWithMixin(PlayerEntity customer, CallbackInfo info) {
        ((MerchantAccessor) customer).setCurrentOfferer(merchantEntity);
        QuestServerPacket.writeS2COffererPacket((ServerPlayerEntity) customer, merchantEntity, ((MerchantAccessor) merchantEntity).getQuestIdList());
    }

    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    private void onDeathMixin(DamageSource source, CallbackInfo info) {
        Quest.failMerchantQuest(merchantEntity, 2);
    }

}
