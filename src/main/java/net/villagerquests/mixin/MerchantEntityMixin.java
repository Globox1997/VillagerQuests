package net.villagerquests.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;
import net.minecraft.world.World;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.data.QuestData;
import net.villagerquests.network.QuestServerPacket;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin extends PassiveEntity implements Merchant, MerchantAccessor {

    private List<Integer> questIdList = new ArrayList<Integer>();
    private List<String> jobList = new ArrayList<String>();
    private List<List<Integer>> oldQuestList = new ArrayList<>();

    public MerchantEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putIntArray("QuestIds", questIdList);

        if (VillagerQuestsMain.CONFIG.rememberQuests) {
            for (int i = 0; i < this.jobList.size(); ++i) {
                String jobString = "OldQuest" + i;
                nbt.putIntArray(jobString, this.oldQuestList.get(i));
                nbt.putString(jobString + "OldVillagerQuest", this.jobList.get(i));
            }
            nbt.putInt("QuestCount", this.jobList.size());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.questIdList.clear();
        this.jobList.clear();
        this.oldQuestList.clear();

        this.questIdList = IntStream.of(nbt.getIntArray("QuestIds")).boxed().collect(Collectors.toList());
        for (Iterator<Integer> iter = this.questIdList.listIterator(); iter.hasNext();) {
            Integer integer = iter.next();
            if (!QuestData.idList.contains(integer)) {
                iter.remove();
            }
        }

        if (VillagerQuestsMain.CONFIG.rememberQuests) {
            for (int i = 0; i < nbt.getInt("QuestCount"); ++i) {
                String jobString = "OldQuest" + i;
                this.jobList.add(nbt.getString(jobString + "OldVillagerQuest"));
                this.oldQuestList.add(IntStream.of(nbt.getIntArray(jobString)).boxed().collect(Collectors.toList()));
            }
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        if (!this.questIdList.isEmpty())
            QuestServerPacket.writeS2CMerchantQuestsPacket(player, (MerchantEntity) (Object) this, this.questIdList);
    }

    @Override
    public void clearQuestList() {
        if (VillagerQuestsMain.CONFIG.rememberQuests && (Object) this instanceof VillagerEntity
                && !this.jobList.contains(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString())) {
            this.jobList.add(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString());
            this.oldQuestList.add(new ArrayList<Integer>(questIdList));
        }
        this.questIdList.clear();
    }

    @Override
    public List<Integer> getQuestIdList() {
        return this.questIdList;
    }

    @Override
    public void setQuestIdList(List<Integer> idList) {
        this.questIdList.clear();
        this.questIdList.addAll(idList);
    }

    @Override
    public List<String> getJobList() {
        return this.jobList;
    }

    @Override
    public List<List<Integer>> getOldQuestList() {
        return this.oldQuestList;
    }

}
