package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestServerPacket;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WanderingTraderEntity;sendOffers(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/text/Text;I)V"))
    private void interactMobMixin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ((MerchantAccessor) player).setCurrentOfferer((MerchantEntity) (Object) this);
        QuestServerPacket.writeS2COffererPacket((ServerPlayerEntity) player, (MerchantEntity) (Object) this);
    }

    @Inject(method = "tickDespawnDelay", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WanderingTraderEntity;discard()V"))
    private void tickDespawnDelayMixin(CallbackInfo info) {
        Quest.failMerchantQuest((WanderingTraderEntity) (Object) this);
    }

    @Override
    public void onDeath(DamageSource source) {
        Quest.failMerchantQuest((WanderingTraderEntity) (Object) this);
        super.onDeath(source);
    }

}
