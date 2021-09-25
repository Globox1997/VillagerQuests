package net.villagerquests.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.MerchantQuests;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin implements MerchantAccessor {
    private final MerchantEntity merchantEntity = (MerchantEntity) (Object) this;
    private boolean settingDataOnRead = false;
    private boolean finishedAQuest = false;

    @Inject(method = "beginTradeWith", at = @At(value = "HEAD"))
    private void beginTradeWithMixin(PlayerEntity customer, CallbackInfo info) {
        ((MerchantAccessor) customer).setCurrentOfferer(merchantEntity);
        QuestServerPacket.writeS2COffererPacket((ServerPlayerEntity) customer, merchantEntity, ((MerchantAccessor) merchantEntity).getQuestIdList());
    }

    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    private void onDeathMixin(DamageSource source, CallbackInfo info) {
        Quest.failMerchantQuest(merchantEntity, 2);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.finishedAQuest = nbt.getBoolean("FinishedAQuest");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("FinishedAQuest", this.finishedAQuest);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult;resultOrPartial(Ljava/util/function/Consumer;)Ljava/util/Optional;", shift = Shift.AFTER))
    private void readCustomDataFromNbtMixinTwo(NbtCompound nbt, CallbackInfo info) {
        this.settingDataOnRead = true;
    }

    @Inject(method = "setVillagerData", at = @At(value = "TAIL"))
    private void setVillagerDataMixin(VillagerData villagerData, CallbackInfo info) {
        if (!merchantEntity.world.isClient && !villagerData.getProfession().equals(VillagerProfession.NONE)) {
            if (this.settingDataOnRead)
                this.settingDataOnRead = false;
            else {
                MerchantQuests.addRandomMerchantQuests(merchantEntity, 1);
                List<Integer> list = ((MerchantAccessor) merchantEntity).getQuestIdList();
                List<? extends PlayerEntity> playerList = merchantEntity.world.getPlayers();
                if (!list.isEmpty() && !playerList.isEmpty())
                    for (int i = 0; i < playerList.size(); i++)
                        QuestServerPacket.writeS2CMerchantQuestsPacket((ServerPlayerEntity) playerList.get(i), merchantEntity, list);
            }
        }
    }

    @Inject(method = "levelUp", at = @At(value = "TAIL"))
    private void levelUpMixin(CallbackInfo info) {
        if (!merchantEntity.world.isClient) {
            MerchantQuests.addRandomMerchantQuests(merchantEntity, 1);
            this.finishedAQuest = false;
        }
    }

    @Inject(method = "canLevelUp", at = @At(value = "HEAD"), cancellable = true)
    private void canLevelUpMixin(CallbackInfoReturnable<Boolean> info) {
        if (!merchantEntity.world.isClient && VillagerQuestsMain.CONFIG.canOnlyLevelUpWhenCompleted && !this.finishedAQuest)
            info.setReturnValue(false);
    }

    @Override
    public void finishedQuest(int questLevel) {
        if (VillagerQuestsMain.CONFIG.canOnlyAddLevelSpecificQuests) {
            if (((VillagerEntity) merchantEntity).getVillagerData().getLevel() == questLevel)
                this.finishedAQuest = true;
        } else
            this.finishedAQuest = true;

    }

}
