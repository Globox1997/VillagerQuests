package net.villagerquests.mixin.ftb;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;

@Mixin(TeamData.class)
public class TeamDataMixin {

    // Can't mixin into PerPlayerData since it is a private inner class
    // AWs work only on vanilla classes/methods/fields
    //
    // @Shadow(remap = false)
    // private boolean locked;

    // // Create new perVillagerPlayerData
    // // private final Object2ObjectOpenHashMap<UUID, TeamData.PerPlayerData> perPlayerVillagerQuestData;
    // private final HashMap<UUID, Long2LongOpenHashMap> villagerQuestPlayerStarted = new HashMap<>();

    // Missing player info
    // @Inject(method = "setStarted", at = @At("HEAD"), cancellable = true, remap = false)
    // private void setStartedMixin(long questId, @Nullable Date time, CallbackInfoReturnable<Boolean> info) {
    // if (!this.locked) {
    // if (time == null) {
    // } else {
    // this.started.put(questId, time.getTime());
    // }
    // }
    // }

    @Inject(method = "canStartTasks", at = @At("HEAD"), cancellable = true, remap = false)
    private void canStartTasksMixin(Quest quest, CallbackInfoReturnable<Boolean> info) {
        if (((QuestAccessor) (Object) quest).isVillagerQuest() && !((QuestAccessor) (Object) quest).isAccepted()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "Ldev/ftb/mods/ftbquests/quest/TeamData;claimReward(Lnet/minecraft/server/network/ServerPlayerEntity;Ldev/ftb/mods/ftbquests/quest/reward/Reward;Z)V", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/reward/Reward;claim(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V"))
    private void claimRewardMixin(ServerPlayerEntity serverPlayerEntity, Reward reward, boolean notify, CallbackInfo info) {
        if ((Object) reward.getQuest() instanceof QuestAccessor questAccessor && questAccessor.isVillagerQuest()) {
            int questMarkType = QuestHelper.getVillagerQuestMarkType(serverPlayerEntity, questAccessor.getVillagerQuestUuid());

            VillagerQuestState.updatePlayerVillagerQuestMarkType(serverPlayerEntity, questAccessor.getVillagerQuestUuid(), questMarkType);
            if (serverPlayerEntity.getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                QuestServerPacket.writeS2CMerchantQuestMarkPacket(serverPlayerEntity, merchantEntity.getId(), questMarkType);
            }
        }
    }

}
