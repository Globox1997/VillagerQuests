package net.villagerquests.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

    // Mixins
    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        // Read
        System.out.println("READ???");
        this.acceptedQuestIdList.clear();
        this.acceptedQuestTraderIdList.clear();
        this.killedMobQuestCount.clear();
        this.finishedQuestIdList.clear();
        this.timerList.clear();
        this.refreshQuestList.clear();

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

    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        // Write
        System.out.println("WRITE???");

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
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (!timerList.isEmpty())
            for (int i = 0; i < timerList.size(); i++)
                if (timerList.get(i) != -1) {
                    timerList.set(i, timerList.get(i) + 1);
                    if (isFailingQuest(this.acceptedQuestIdList.get(timerList.indexOf(i)), timerList.get(i)))
                        failPlayerQuest(this.acceptedQuestIdList.get(timerList.indexOf(i)));
                }

        if (!refreshQuestList.isEmpty())
            for (int u = 0; u < refreshQuestList.size(); u++)
                if (refreshQuestList.get(u) != -1) {
                    refreshQuestList.set(u, refreshQuestList.get(u) + 1);
                    if (canRefreshQuest(finishedQuestIdList.get(refreshQuestList.indexOf(u)), refreshQuestList.get(u)))
                        refreshQuest(refreshQuestList.indexOf(u));
                }

    }

    // Only called on Server
    @Inject(method = "onKilledOther", at = @At(value = "TAIL"))
    private void onKilledOtherMixin(ServerWorld world, LivingEntity other, CallbackInfo info) {
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
    public boolean isOriginalQuestGiver(UUID uuid, int questId) {
        if (this.acceptedQuestIdList.contains(questId) && this.acceptedQuestTraderIdList.get(this.acceptedQuestIdList.indexOf(questId)).equals(uuid))
            return true;
        else
            return false;
    }

    @Override
    public void finishPlayerQuest(int id) {
        Quest quest = Quest.getQuestById(id);
        System.out.println(quest);
        this.finishedQuestIdList.add(id);
        this.refreshQuestList.add(quest.getQuestRefreshTimer());

        this.acceptedQuestTraderIdList.remove(this.acceptedQuestIdList.indexOf(id));
        this.killedMobQuestCount.remove(this.acceptedQuestIdList.indexOf(id));
        this.timerList.remove(this.acceptedQuestIdList.indexOf(id));
        this.acceptedQuestIdList.remove(this.acceptedQuestIdList.indexOf(id));
        if (!playerEntity.world.isClient) {
            quest.consumeCompletedQuestItems(playerEntity);
            quest.getRewards(playerEntity);
        }
    }

    @Override
    public void failPlayerQuest(int id) {
        this.finishedQuestIdList.add(id);
        this.refreshQuestList.add(Quest.getQuestById(id).getQuestRefreshTimer());

        this.acceptedQuestTraderIdList.remove(this.acceptedQuestIdList.indexOf(id));
        this.killedMobQuestCount.remove(this.acceptedQuestIdList.indexOf(id));
        this.timerList.remove(this.acceptedQuestIdList.indexOf(id));
        this.acceptedQuestIdList.remove(id);

        // Send info for failing
    }

    @Override
    public void addPlayerQuestId(int id, UUID uuid) {
        if (this.acceptedQuestIdList.isEmpty() || !this.acceptedQuestIdList.contains(id)) {
            this.acceptedQuestIdList.add(id);
            this.acceptedQuestTraderIdList.add(uuid);
            this.killedMobQuestCount.add(Quest.getQuestById(id).getKillTaskEntityIds());
            this.timerList.add(Quest.getQuestById(id).getQuestTimer());
            System.out.println("Add Quest: " + this.acceptedQuestIdList);
        }
        if (this.finishedQuestIdList.contains(id)) {
            this.refreshQuestList.remove(this.finishedQuestIdList.indexOf(id));
            this.finishedQuestIdList.remove(id);
        }
        System.out.println("Quest got Added?");
    }

    @Override
    public void syncPlayerQuest(List<Integer> questIds, List<List<Integer>> killedCount, List<UUID> traderUuids, List<Integer> finishedIds, List<Integer> timers, List<Integer> refresher) {
        this.acceptedQuestIdList = questIds;
        this.killedMobQuestCount = killedCount;
        this.acceptedQuestTraderIdList = traderUuids;
        this.finishedQuestIdList = finishedIds;
        this.timerList = timers;
        this.refreshQuestList = refresher;
    }

    // Methods used in this class

    private void refreshQuest(int index) {
        this.finishedQuestIdList.remove(index);
        this.refreshQuestList.remove(index);
    }

    private boolean isFailingQuest(int id, int time) {
        if (Quest.getQuestById(id).getQuestTimer() <= time)
            return true;
        else
            return false;
    }

    private boolean canRefreshQuest(int id, int time) {
        if (Quest.getQuestById(id).getQuestRefreshTimer() <= time)
            return true;
        else
            return false;
    }

}
