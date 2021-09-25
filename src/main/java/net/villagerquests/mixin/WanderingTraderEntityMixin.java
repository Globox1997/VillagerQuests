package net.villagerquests.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.MerchantQuests;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    private final MerchantEntity merchantEntity = (MerchantEntity) (Object) this;

    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WanderingTraderEntity;sendOffers(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/text/Text;I)V"))
    private void interactMobMixin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ((MerchantAccessor) player).setCurrentOfferer(merchantEntity);
        QuestServerPacket.writeS2COffererPacket((ServerPlayerEntity) player, merchantEntity, ((MerchantAccessor) merchantEntity).getQuestIdList());
    }

    @Inject(method = "tickDespawnDelay", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WanderingTraderEntity;discard()V"))
    private void tickDespawnDelayMixin(CallbackInfo info) {
        Quest.failMerchantQuest(merchantEntity, 1);
    }

    @Override
    public void onDeath(DamageSource source) {
        Quest.failMerchantQuest(merchantEntity, 2);
        super.onDeath(source);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        MerchantQuests.addRandomMerchantQuests(merchantEntity, VillagerQuestsMain.CONFIG.wanderingQuestQuantity);
        return super.initialize(world, difficulty, spawnReason, (EntityData) entityData, entityNbt);
    }

}
