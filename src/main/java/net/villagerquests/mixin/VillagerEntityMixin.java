package net.villagerquests.mixin;

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
    private int newQuestTicker;

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
        this.newQuestTicker = nbt.getInt("NewQuestTicker");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("FinishedAQuest", this.finishedAQuest);
        nbt.putInt("NewQuestTicker", this.newQuestTicker);
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

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (!this.merchantEntity.world.isClient) {
            if (this.newQuestTicker >= 0)
                this.newQuestTicker--;
            if (this.newQuestTicker == 1)
                MerchantQuests.addRandomMerchantQuests(merchantEntity, 1);
        }
    }

    @Override
    public void finishedQuest(int questLevel) {
        if (VillagerQuestsMain.CONFIG.canOnlyAddLevelSpecificQuests) {
            if (((VillagerEntity) merchantEntity).getVillagerData().getLevel() == questLevel)
                addAndFinishQuestTicker();
        } else
            addAndFinishQuestTicker();

    }

    private void addAndFinishQuestTicker() {
        this.finishedAQuest = true;
        if (this.newQuestTicker > 1)
            this.newQuestTicker -= this.newQuestTicker / 10;
        else
            this.newQuestTicker = VillagerQuestsMain.CONFIG.newQuestTimer + this.merchantEntity.world.random.nextInt(VillagerQuestsMain.CONFIG.newQuestTimer);
    }

}
