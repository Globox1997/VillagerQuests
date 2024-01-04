package net.villagerquests.mixin.ftb;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.access.TeamDataAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;

@Mixin(TeamData.class)
public class TeamDataMixin implements TeamDataAccessor {

    private boolean acceptQuest = false;
    private HashMap<Long, Long> timer;

    @Shadow(remap = false)
    @Mutable
    @Final
    private BaseQuestFile file;
    @Shadow(remap = false)
    @Mutable
    @Final
    private Long2LongOpenHashMap started;
    @Shadow(remap = false)
    @Mutable
    @Final
    private Long2LongOpenHashMap completed;
    @Shadow(remap = false)
    @Mutable
    @Final
    private UUID teamId;

    @Inject(method = "Ldev/ftb/mods/ftbquests/quest/TeamData;<init>(Ljava/util/UUID;Ldev/ftb/mods/ftbquests/quest/BaseQuestFile;Ljava/lang/String;)V", at = @At("TAIL"), remap = false)
    private void initMixin(UUID teamId, BaseQuestFile file, String name, CallbackInfo info) {
        this.acceptQuest = false;
        this.timer = new HashMap<Long, Long>();
    }

    @Inject(method = "canStartTasks", at = @At("HEAD"), cancellable = true, remap = false)
    private void canStartTasksMixin(Quest quest, CallbackInfoReturnable<Boolean> info) {
        if (((QuestAccessor) (Object) quest).isVillagerQuest() && !this.isStarted(quest)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "Ldev/ftb/mods/ftbquests/quest/TeamData;claimReward(Lnet/minecraft/server/network/ServerPlayerEntity;Ldev/ftb/mods/ftbquests/quest/reward/Reward;Z)V", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/reward/Reward;claim(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V"))
    private void claimRewardMixin(ServerPlayerEntity serverPlayerEntity, Reward reward, boolean notify, CallbackInfo info) {
        if ((Object) reward.getQuest() instanceof QuestAccessor questAccessor && questAccessor.isVillagerQuest()) {
            int questMarkType = QuestHelper.getVillagerQuestMarkType(serverPlayerEntity, questAccessor.getVillagerQuestUuid());

            VillagerQuestState.updatePlayerVillagerQuestMarkType(serverPlayerEntity.getServer(), serverPlayerEntity.getUuid(), questAccessor.getVillagerQuestUuid(), questMarkType);
            if (serverPlayerEntity.getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                QuestServerPacket.writeS2CMerchantQuestMarkPacket(serverPlayerEntity, merchantEntity.getId(), questMarkType);
            }
        }
    }

    @Inject(method = "setStarted", at = @At("HEAD"), cancellable = true, remap = false)
    private void setStartedMixin(long questId, @Nullable Date time, CallbackInfoReturnable<Boolean> info) {
        if (file.getQuest(questId) != null) {
            if (!this.getOnlineMembers().isEmpty()) {
                if (time == null) {
                    this.timer.remove(questId);
                } else {
                    this.timer.put(questId, this.getOnlineMembers().stream().toList().get(0).getServer().getOverworld().getTime());
                }
            }
            if (time != null && ((QuestAccessor) (Object) file.getQuest(questId)).isVillagerQuest()) {
                if (!this.acceptQuest) {
                    info.setReturnValue(false);
                } else {
                    this.acceptQuest = false;
                }
            }
        }
    }

    @Inject(method = "setCompleted", at = @At("HEAD"), cancellable = true, remap = false)
    private void setCompletedMixin(long id, @Nullable Date time, CallbackInfoReturnable<Boolean> info) {
        if (file.getQuest(id) != null) {
            this.timer.remove(id);
        }
    }

    // Sync villager quests
    @Inject(method = "mergeData", at = @At("TAIL"), remap = false)
    private void mergeDataMixin(TeamData from, CallbackInfo info) {
        List<ServerPlayerEntity> onlinePlayers = this.getOnlineMembers().stream().toList();
        if (!onlinePlayers.isEmpty()) {
            MinecraftServer server = onlinePlayers.get(0).getServer();

            this.started.forEach((questId, data) -> {
                if (!((TeamDataAccessor) from).getStarted().containsKey((long) questId) && (Object) this.file.getQuest(questId) instanceof QuestAccessor questAccessor
                        && questAccessor.isVillagerQuest()) {
                    Iterator<UUID> iterator = FTBTeamsAPI.api().getManager().getTeamByID(teamId).get().getMembers().iterator();
                    while (iterator.hasNext()) {
                        UUID playeUuid = iterator.next();
                        int questMarkType = -1;
                        if (server.getPlayerManager().getPlayer(playeUuid) != null) {
                            questMarkType = QuestHelper.getVillagerQuestMarkType(server.getPlayerManager().getPlayer(playeUuid), questAccessor.getVillagerQuestUuid());
                            if (server.getPlayerManager().getPlayer(playeUuid).getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                                QuestServerPacket.writeS2CMerchantQuestMarkPacket(server.getPlayerManager().getPlayer(playeUuid), merchantEntity.getId(), questMarkType);
                            }
                        }
                        VillagerQuestState.updatePlayerVillagerQuestMarkType(server, playeUuid, questAccessor.getVillagerQuestUuid(), questMarkType);
                    }
                }
            });
            this.completed.forEach((questId, data) -> {
                if (!((TeamDataAccessor) from).getCompleted().containsKey((long) questId) && (Object) this.file.getQuest(questId) instanceof QuestAccessor questAccessor
                        && questAccessor.isVillagerQuest()) {
                    Iterator<UUID> iterator = FTBTeamsAPI.api().getManager().getTeamByID(teamId).get().getMembers().iterator();
                    while (iterator.hasNext()) {
                        UUID playeUuid = iterator.next();
                        int questMarkType = -1;
                        if (server.getPlayerManager().getPlayer(playeUuid) != null) {
                            questMarkType = QuestHelper.getVillagerQuestMarkType(server.getPlayerManager().getPlayer(playeUuid), questAccessor.getVillagerQuestUuid());
                            if (server.getPlayerManager().getPlayer(playeUuid).getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                                QuestServerPacket.writeS2CMerchantQuestMarkPacket(server.getPlayerManager().getPlayer(playeUuid), merchantEntity.getId(), questMarkType);
                            }
                        }
                        VillagerQuestState.updatePlayerVillagerQuestMarkType(server, playeUuid, questAccessor.getVillagerQuestUuid(), questMarkType);
                    }
                }
            });
        }
    }

    @Inject(method = "serializeNBT", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void serializeNBTMixin(CallbackInfoReturnable<SNBTCompoundTag> info, SNBTCompoundTag snbtCompoundTag) {
        SNBTCompoundTag timerProgressNBT = new SNBTCompoundTag();
        Iterator<Map.Entry<Long, Long>> iterator = this.timer.entrySet().iterator();
        timerProgressNBT.putInt("Size", this.timer.size());
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            timerProgressNBT.putLong("QuestLong" + count, entry.getKey());
            timerProgressNBT.putLong("QuestTime" + count, entry.getValue());
        }
        snbtCompoundTag.put("Timer", timerProgressNBT);
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"), remap = false)
    private void deserializeNBTMixin(SNBTCompoundTag nbt, CallbackInfo info) {
        this.timer.clear();
        NbtCompound timerProgressNBT = nbt.getCompound("Timer");
        for (int i = 0; i < timerProgressNBT.getInt("Size"); i++) {
            this.timer.put(timerProgressNBT.getLong("QuestLong" + i), timerProgressNBT.getLong("QuestTime" + i));
        }
    }

    @Inject(method = "write", at = @At("TAIL"), remap = false)
    private void writeMixin(PacketByteBuf buffer, boolean self, CallbackInfo info) {
        buffer.writeInt(this.timer.size());
        Iterator<Map.Entry<Long, Long>> iterator = this.timer.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            buffer.writeLong(entry.getKey());
            buffer.writeLong(entry.getValue());
        }
    }

    @Inject(method = "read", at = @At("TAIL"), remap = false)
    private void readMixin(PacketByteBuf buffer, boolean self, CallbackInfo info) {
        this.timer.clear();
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            this.timer.put(buffer.readLong(), buffer.readLong());
        }
    }

    // If time is null, the quest will get removed from started
    @Override
    public void setQuestStarted(long questId, @Nullable Date time) {
        this.acceptQuest = true;
        setStarted(questId, time);
    }

    @Override
    public Long2LongOpenHashMap getStarted() {
        return this.started;
    }

    @Override
    public Long2LongOpenHashMap getCompleted() {
        return this.completed;
    }

    @Override
    public HashMap<Long, Long> getTimer() {
        return this.timer;
    }

    @Shadow(remap = false)
    public boolean setStarted(long questId, @Nullable Date time) {
        return false;
    }

    @Shadow(remap = false)
    public boolean isStarted(QuestObject object) {
        return false;
    }

    @Shadow(remap = false)
    public Collection<ServerPlayerEntity> getOnlineMembers() {
        return null;
    }

}
