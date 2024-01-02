package net.villagerquests.mixin.ftb;

import java.util.Iterator;
import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;

@SuppressWarnings("unused")
@Mixin(Task.class)
public class TaskMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    private Quest quest;

    @Inject(method = "onCompleted", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/Quest;onCompleted(Ldev/ftb/mods/ftbquests/events/QuestProgressEventData;)V"), cancellable = true, remap = false)
    private final void onCompletedMixin(QuestProgressEventData<?> data, CallbackInfo info) {
        if ((Object) quest instanceof QuestAccessor questAccessor && questAccessor.isVillagerQuest()) {
            if (data.getOnlineMembers().size() > 0) {
                MinecraftServer server = data.getOnlineMembers().get(0).getServer();
                Iterator<UUID> iterator = FTBTeamsAPI.api().getManager().getTeamByID(data.getTeamData().getTeamId()).get().getMembers().iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    int questMarkType = 2;

                    if (server.getPlayerManager().getPlayer(uuid) != null) {
                        if (server.getPlayerManager().getPlayer(uuid).getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                            QuestServerPacket.writeS2CMerchantQuestMarkPacket(server.getPlayerManager().getPlayer(uuid), merchantEntity.getId(), questMarkType);
                        }
                    }
                    VillagerQuestState.updatePlayerVillagerQuestMarkType(server, uuid, questAccessor.getVillagerQuestUuid(), questMarkType);
                }
            }

            info.cancel();
        }
    }

    // private boolean villagerQuest;
    // private UUID villagerUuid;

    // @Inject(method = "<init>", at = @At("TAIL"))
    // private void initMixin(long id, Quest quest, CallbackInfo info) {
    // villagerQuest = false;
    // villagerUuid = null;
    // }

    // @Inject(method = "writeData", at = @At("TAIL"))
    // private void writeDataMixin(NbtCompound nbt, CallbackInfo info) {
    // nbt.putBoolean("villagerquest", villagerQuest);
    // if (villagerQuest) {
    // nbt.putUuid("villageruuid", villagerUuid);
    // }
    // }

    // @Inject(method = "readData", at = @At("TAIL"))
    // private void readDataMixin(NbtCompound nbt, CallbackInfo info) {
    // villagerQuest = nbt.getBoolean("villagerquest");
    // if (villagerQuest) {
    // villagerUuid = nbt.getUuid("villageruuid");
    // }
    // }

    // @Inject(method = "writeNetData", at = @At("TAIL"))
    // private void writeNetDataMixin(PacketByteBuf buffer, CallbackInfo info) {
    // buffer.writeBoolean(villagerQuest);
    // if (villagerQuest) {
    // buffer.writeUuid(villagerUuid);
    // }
    // }

    // @Inject(method = "readNetData", at = @At("TAIL"))
    // private void readNetDataMixin(PacketByteBuf buffer, CallbackInfo info) {
    // villagerQuest = buffer.readBoolean();
    // if (villagerQuest) {
    // villagerUuid = buffer.readUuid();
    // }
    // }

    // @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    // private void fillConfigGroupMixin(ConfigGroup config, CallbackInfo info) {
    // config.addBool("villager_quest", villagerQuest, v -> villagerQuest = v, false).setNameKey("ftbquests.quest.misc.villager_quest");
    // config.addString("villager_uuid", villagerUuid != null ? villagerUuid.toString() : "", v -> {
    // try {
    // v = UUID.fromString(v).toString();
    // villagerUuid = UUID.fromString(v);
    // } catch (IllegalArgumentException illegalArgumentException) {
    // v = "";
    // villagerUuid = null;
    // }
    // }, "").setNameKey("ftbquests.quest.misc.villager_uuid");
    // }

}
