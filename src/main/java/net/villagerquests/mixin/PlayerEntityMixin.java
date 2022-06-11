package net.villagerquests.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestServerPacket;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements MerchantAccessor, PlayerAccessor {

    private final PlayerEntity playerEntity = (PlayerEntity) (Object) this;
    public MerchantEntity tradingEntity;

    private List<Integer> acceptedQuestIdList = new ArrayList<>();
    private List<UUID> acceptedQuestTraderIdList = new ArrayList<>();
    private List<List<Integer>> killedMobQuestCount = new ArrayList<>();
    private List<Integer> finishedQuestIdList = new ArrayList<>();
    private List<Integer> timerList = new ArrayList<>();
    private List<Integer> refreshQuestList = new ArrayList<>();
    public List<List<Object>> travelIdList = new ArrayList<>();

    // Mixins
    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.acceptedQuestIdList.clear();
        this.acceptedQuestTraderIdList.clear();
        this.killedMobQuestCount.clear();
        this.finishedQuestIdList.clear();
        this.timerList.clear();
        this.refreshQuestList.clear();
        this.travelIdList.clear();

        this.acceptedQuestIdList = IntStream.of(nbt.getIntArray("AcceptedQuests")).boxed().collect(Collectors.toList());

        this.finishedQuestIdList = IntStream.of(nbt.getIntArray("FinishedQuests")).boxed().collect(Collectors.toList());
        this.timerList = IntStream.of(nbt.getIntArray("QuestTimer")).boxed().collect(Collectors.toList());
        this.refreshQuestList = IntStream.of(nbt.getIntArray("RefreshQuestsTimer")).boxed().collect(Collectors.toList());

        for (int i = 0; i < nbt.getInt("QuestCountNumber"); i++) {
            this.killedMobQuestCount.add(IntStream.of(nbt.getIntArray("KilledMobQuestList" + i)).boxed().collect(Collectors.toList()));
        }
        for (int i = 0; i < nbt.getInt("TraderUUIDCountNumber"); i++) {
            this.acceptedQuestTraderIdList.add(nbt.getUuid("TraderUUID" + i));
        }
        for (int i = 0; i < nbt.getInt("TravelCountNumber"); i++) {
            List<Object> collectTravelIdList = new ArrayList<>();
            for (int u = 0; u < 10; u++) {
                if (nbt.getString("TravelId" + i + "" + u * 2) != "") {
                    collectTravelIdList.add(nbt.getString("TravelId" + i + "" + u * 2));
                    if (nbt.getString("TravelId" + i + "" + u * 2 + 1).equals("true"))
                        collectTravelIdList.add(true);
                    else
                        collectTravelIdList.add(false);
                } else {
                    this.travelIdList.add(collectTravelIdList);
                    break;
                }
            }
        }

    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putIntArray("AcceptedQuests", this.acceptedQuestIdList);
        nbt.putIntArray("FinishedQuests", this.finishedQuestIdList);
        nbt.putIntArray("QuestTimer", this.timerList);
        nbt.putIntArray("RefreshQuestsTimer", this.refreshQuestList);
        for (int i = 0; i < this.killedMobQuestCount.size(); i++) {
            nbt.putIntArray("KilledMobQuestList" + i, this.killedMobQuestCount.get(i));
        }
        nbt.putInt("QuestCountNumber", this.killedMobQuestCount.size());
        for (int u = 0; u < this.acceptedQuestTraderIdList.size(); u++) {
            nbt.putUuid("TraderUUID" + u, this.acceptedQuestTraderIdList.get(u));
        }
        nbt.putInt("TraderUUIDCountNumber", this.acceptedQuestTraderIdList.size());

        for (int i = 0; i < this.travelIdList.size(); i++) {
            if (!this.travelIdList.get(i).isEmpty())
                for (int u = 0; u < this.travelIdList.get(i).size(); u++)
                    nbt.putString("TravelId" + i + "" + u, String.valueOf(this.travelIdList.get(i).get(u)));
        }
        nbt.putInt("TravelCountNumber", this.travelIdList.size());
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (!this.timerList.isEmpty())
            for (int i = 0; i < this.timerList.size(); i++)
                if (!this.timerList.get(i).equals(-1)) {
                    this.timerList.set(i, this.timerList.get(i) - 1);
                    if (isFailingOrRefreshQuest(this.timerList.get(i)))
                        failPlayerQuest(this.acceptedQuestIdList.get(this.timerList.indexOf(this.timerList.get(i))), 0);
                }

        if (!this.refreshQuestList.isEmpty())
            for (int u = 0; u < this.refreshQuestList.size(); u++)
                if (!this.refreshQuestList.get(u).equals(-1)) {
                    this.refreshQuestList.set(u, this.refreshQuestList.get(u) - 1);
                    if (isFailingOrRefreshQuest(this.refreshQuestList.get(u)))
                        refreshQuest(this.refreshQuestList.indexOf(this.refreshQuestList.get(u)));
                }
    }

    // Only called on Server
    @Inject(method = "onKilledOther", at = @At(value = "TAIL"))
    private void onKilledOtherMixin(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> info) {
        // Instead of sending multiple packets, send one with all information
        int entityRawId = Registry.ENTITY_TYPE.getRawId(other.getType());
        if (this.canAddKilledMobQuestCount(entityRawId))
            QuestServerPacket.writeS2CQuestKillAdditionPacket((ServerPlayerEntity) (Object) this, entityRawId);
    }

    // Interface usage
    @Override
    public boolean canAddKilledMobQuestCount(int entityRawId) {
        boolean isExistingRawId = false;
        for (int i = 0; i < this.killedMobQuestCount.size(); i++) {
            for (int u = 0; u < this.killedMobQuestCount.get(i).size() / 2; u++) {
                if (this.killedMobQuestCount.get(i).get(u * 2).equals(entityRawId)) {
                    this.killedMobQuestCount.get(i).set(u * 2 + 1, this.killedMobQuestCount.get(i).get(u * 2 + 1) + 1);
                    isExistingRawId = true;
                }
            }
        }

        return isExistingRawId;
    }

    @Override
    public void setCurrentOfferer(MerchantEntity merchantEntity) {
        this.tradingEntity = merchantEntity;
    }

    @Override
    public MerchantEntity getCurrentOfferer() {
        return this.tradingEntity;
    }

    @Override
    public List<Integer> getPlayerQuestIdList() {
        return this.acceptedQuestIdList;
    }

    @Override
    public List<List<Integer>> getPlayerKilledQuestList() {
        return this.killedMobQuestCount;
    }

    @Override
    public List<UUID> getPlayerQuestTraderIdList() {
        return this.acceptedQuestTraderIdList;
    }

    @Override
    public List<Integer> getPlayerFinishedQuestIdList() {
        return this.finishedQuestIdList;
    }

    @Override
    public List<Integer> getPlayerQuestTimerList() {
        return this.timerList;
    }

    @Override
    public List<Integer> getPlayerQuestRefreshTimerList() {
        return this.refreshQuestList;
    }

    @Override
    public List<List<Object>> getPlayerTravelList() {
        return this.travelIdList;
    }

    @Override
    public boolean isOriginalQuestGiver(UUID uuid, int questId) {
        if (this.acceptedQuestIdList.contains(questId) && this.acceptedQuestTraderIdList.get(this.acceptedQuestIdList.indexOf(questId)).equals(uuid))
            return true;
        else
            return false;
    }

    @Override
    public void finishPlayerQuest(int id) {
        Quest quest = Quest.getQuestById(id);
        this.finishedQuestIdList.add(id);
        this.refreshQuestList.add(quest.getQuestRefreshTimer());

        this.acceptedQuestTraderIdList.remove(this.acceptedQuestIdList.indexOf(id));
        this.killedMobQuestCount.remove(this.acceptedQuestIdList.indexOf(id));
        this.travelIdList.remove(this.acceptedQuestIdList.indexOf(id));
        this.timerList.remove(this.acceptedQuestIdList.indexOf(id));
        this.acceptedQuestIdList.remove(this.acceptedQuestIdList.indexOf(id));
        if (!playerEntity.world.isClient) {
            quest.consumeCompletedQuestItems(playerEntity);
            quest.getRewards(playerEntity);
        }
    }

    @Override
    public void failPlayerQuest(int id, int reason) {
        this.finishedQuestIdList.add(id);
        Quest quest = Quest.getQuestById(id);
        this.refreshQuestList.add(quest.getQuestRefreshTimer());

        int index = this.acceptedQuestIdList.indexOf(id);
        this.acceptedQuestTraderIdList.remove(index);
        this.killedMobQuestCount.remove(index);
        this.travelIdList.remove(index);
        this.timerList.remove(index);
        this.acceptedQuestIdList.remove(index);

        if (!playerEntity.world.isClient) {
            if (reason == 0)
                playerEntity.sendMessage(Text.translatable("text.villagerquests.questTimeout", quest.getTitle()), true);
            else if (reason == 1)
                playerEntity.sendMessage(Text.translatable("text.villagerquests.questGiverDespawn", quest.getTitle()), true);
            else if (reason == 2)
                playerEntity.sendMessage(Text.translatable("text.villagerquests.questGiverDied", quest.getTitle()), true);
        }

    }

    @Override
    public void removeQuest(int id) {
        if (this.finishedQuestIdList.contains(id)) {
            this.refreshQuestList.remove(this.finishedQuestIdList.indexOf(id));
            this.finishedQuestIdList.remove(this.finishedQuestIdList.indexOf(id));
        }
        int index = this.acceptedQuestIdList.indexOf(id);
        if (index != -1) {
            this.acceptedQuestTraderIdList.remove(index);
            this.killedMobQuestCount.remove(index);
            this.travelIdList.remove(index);
            this.timerList.remove(index);
            this.acceptedQuestIdList.remove(index);
        }
        if (!playerEntity.world.isClient) {
            playerEntity.sendMessage(Text.translatable("text.villagerquests.questCommandRemoval", id), false);
        }
    }

    @Override
    public void addPlayerQuestId(int id, UUID uuid) {
        if (this.acceptedQuestIdList.isEmpty() || !this.acceptedQuestIdList.contains(id)) {
            this.acceptedQuestIdList.add(id);
            this.acceptedQuestTraderIdList.add(uuid);
            this.killedMobQuestCount.add(Quest.getQuestById(id).getKillTaskEntityIds());
            this.timerList.add(Quest.getQuestById(id).getQuestTimer());
            this.travelIdList.add(Quest.getQuestById(id).getTravelTaskIds());
        }

        if (this.finishedQuestIdList.contains(id)) {
            this.refreshQuestList.remove(this.finishedQuestIdList.indexOf(id));
            this.finishedQuestIdList.remove(this.finishedQuestIdList.indexOf(id));
        }
    }

    @Override
    public void syncPlayerQuest(List<Integer> questIds, List<List<Integer>> killedCount, List<List<Object>> travelIds, List<UUID> traderUuids, List<Integer> finishedIds, List<Integer> timers,
            List<Integer> refresher) {
        this.acceptedQuestIdList = questIds;
        this.killedMobQuestCount = killedCount;
        this.acceptedQuestTraderIdList = traderUuids;
        this.finishedQuestIdList = finishedIds;
        this.timerList = timers;
        this.refreshQuestList = refresher;
        this.travelIdList = travelIds;
    }

    // Methods used in this class

    private void refreshQuest(int index) {
        this.finishedQuestIdList.remove(index);
        this.refreshQuestList.remove(index);
    }

    private boolean isFailingOrRefreshQuest(int time) {
        if (time <= 0) {
            return true;
        } else
            return false;
    }

}
