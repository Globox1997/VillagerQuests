package net.villagerquests.mixin;

import java.util.ArrayList;
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
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
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
        if (VillagerQuestsMain.CONFIG.rememberQuests) {
            for (int i = 0; i < nbt.getInt("QuestCount"); ++i) {
                String jobString = "OldQuest" + i;
                this.jobList.add(nbt.getString(jobString + "OldVillagerQuest"));
                this.oldQuestList.add(IntStream.of(nbt.getIntArray(jobString)).boxed().collect(Collectors.toList()));
            }
        }
    }

    @Inject(method = "fillRecipesFromPool", at = @At(value = "HEAD"))
    protected void fillRecipesFromPoolMixin(TradeOfferList recipeList, TradeOffers.Factory[] pool, int count, CallbackInfo info) {
        if (!this.world.isClient) {
            boolean isVillager = (Object) this instanceof VillagerEntity;
            if (isVillager && questIdList.isEmpty() && !jobList.isEmpty() && this.jobList.contains(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString())
                    && VillagerQuestsMain.CONFIG.rememberQuests) {
                questIdList.addAll(this.oldQuestList.get(this.jobList.indexOf(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString())));
            } else {
                List<Integer> availableQuests = new ArrayList<>();
                if (isVillager) {
                    VillagerData villagerData = ((VillagerEntity) (Object) this).getVillagerData();
                    for (int i = 0; i < QuestData.idList.size(); i++) {
                        if (villagerData.getProfession().getId().equals(QuestData.professionList.get(i)) && villagerData.getLevel() >= QuestData.levelList.get(i)
                                && !questIdList.contains(QuestData.idList.get(i)))
                            availableQuests.add(QuestData.idList.get(i));
                    }
                } else {
                    // Wandering trader
                    for (int i = 0; i < QuestData.idList.size(); i++) {
                        if (QuestData.professionList.get(i).equals("wandering_trader"))
                            availableQuests.add(QuestData.idList.get(i));
                    }
                }
                if (availableQuests.size() > count) {
                    int questAdder = 0;
                    while (questAdder < count) {
                        int randomInt = random.nextInt(availableQuests.size());
                        if (!questIdList.contains(availableQuests.get(randomInt))) {
                            questIdList.add(availableQuests.get(randomInt));
                            questAdder++;
                        }
                    }
                } else {
                    questIdList.addAll(availableQuests);
                }
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
        if (VillagerQuestsMain.CONFIG.rememberQuests && (Object) this instanceof VillagerEntity) {
            jobList.add(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString());
            oldQuestList.add(questIdList);
        }
        questIdList.clear();
    }

    @Override
    public List<Integer> getQuestIdList() {
        return this.questIdList;
    }

    @Override
    public void setQuestIdList(List<Integer> idList) {
        this.questIdList.addAll(idList);
    }

}
