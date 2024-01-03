package net.villagerquests.mixin;

import java.util.HashMap;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.init.ConfigInit;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin extends PassiveEntity implements MerchantAccessor {

    private static final TrackedData<Integer> TALK_TIME_LEFT = DataTracker.registerData(MerchantEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private int offererGlowTime = 0;
    private int questMarkType = 0;
    private boolean changeableName = true;
    private boolean offersTrades = true;

    public MerchantEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTrackerMixin(CallbackInfo info) {
        this.dataTracker.startTracking(TALK_TIME_LEFT, 0);
    }

    @Inject(method = "setCustomer", at = @At("HEAD"))
    private void setCustomerMixin(@Nullable PlayerEntity customer, CallbackInfo info) {
        if (customer != null) {
            ((MerchantAccessor) customer).setCurrentOfferer((MerchantEntity) (Object) this);
            if (!customer.getWorld().isClient()) {
                QuestServerPacket.writeS2COffererPacket((ServerPlayerEntity) customer, this.getId());
            }
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.changeableName = nbt.getBoolean("ChangeableName");
        this.offersTrades = nbt.getBoolean("OffersTrades");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("ChangeableName", this.changeableName);
        nbt.putBoolean("OffersTrades", this.offersTrades);
    }

    @Inject(method = "onDeath", at = @At(value = "TAIL"))
    private void onDeathMixin(DamageSource damageSource, CallbackInfo info) {
        if (!this.getWorld().isClient()) {
            QuestHelper.removeVillagerQuestFromQuest(this.getUuid());
            VillagerQuestState.removeUuidFromServerVillagerQuestState(this.getWorld().getServer(), this.getUuid());
        } else {
            ((MerchantAccessor) this).setQuestMarkType(0);
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        int questMarkType = 0;
        HashMap<UUID, Integer> merchantQuestMarkMap = VillagerQuestState.getPlayerVillagerQuestState(player.getServer(), player.getUuid()).getMerchantQuestMarkMap();
        if (merchantQuestMarkMap.containsKey(this.getUuid())) {
            questMarkType = merchantQuestMarkMap.get(this.getUuid());
        } else {
            questMarkType = QuestHelper.getVillagerQuestMarkType(player, this.getUuid());
            merchantQuestMarkMap.put(this.getUuid(), questMarkType);
        }
        if (questMarkType != 0) {
            QuestServerPacket.writeS2CMerchantQuestMarkPacket(player, this.getId(), questMarkType);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient() && this.offererGlowTime != 0 && (int) this.getWorld().getTime() > this.offererGlowTime + ConfigInit.CONFIG.villagerQuestGlowTime) {
            this.offererGlowTime = 0;
            this.setFlag(6, false);
        }
        if (this.getTalkTime() > 0) {
            this.setTalkTime(this.getTalkTime() - 1);
        }
    }

    @Override
    public void setCustomName(Text name) {
        if (this.changeableName) {
            super.setCustomName(name);
        }
    }

    @Override
    public void setQuestMarkType(int questMarkType) {
        this.questMarkType = questMarkType;
    }

    @Override
    public int getQuestMarkType() {
        return this.questMarkType;
    }

    @Override
    public void setOffererGlow() {
        this.offererGlowTime = (int) this.getWorld().getTime();
        this.setFlag(6, true);
    }

    @Override
    public void setChangeableName(boolean changeableName) {
        this.changeableName = changeableName;
    }

    @Override
    public boolean getChangeableName() {
        return this.changeableName;
    }

    @Override
    public void setOffersTrades(boolean offersTrades) {
        this.offersTrades = offersTrades;
    }

    @Override
    public boolean getOffersTrades() {
        return this.offersTrades;
    }

    @Override
    public void setTalkTime(int talkTime) {
        this.dataTracker.set(TALK_TIME_LEFT, talkTime);
    }

    @Override
    public int getTalkTime() {
        return this.dataTracker.get(TALK_TIME_LEFT);
    }

}
