package net.villagerquests.mixin.ftb;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;

@Mixin(TeamData.class)
public class TeamDataMixin {

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

            VillagerQuestState.updatePlayerVillagerQuestMarkType(serverPlayerEntity.getServer(), serverPlayerEntity.getUuid(), questAccessor.getVillagerQuestUuid(), questMarkType);
            if (serverPlayerEntity.getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
                QuestServerPacket.writeS2CMerchantQuestMarkPacket(serverPlayerEntity, merchantEntity.getId(), questMarkType);
            }
        }
    }

}
