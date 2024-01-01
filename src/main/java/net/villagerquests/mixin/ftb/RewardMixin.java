package net.villagerquests.mixin.ftb;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.villagerquests.access.QuestAccessor;

@Mixin(Reward.class)
public class RewardMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    protected Quest quest;

    @Shadow(remap = false)
    private boolean excludeFromClaimAll;

    // Not sure if required or even good
    @Inject(method = "getExcludeFromClaimAll", at = @At("HEAD"), cancellable = true, remap = false)
    private void getExcludeFromClaimAllMixin(CallbackInfoReturnable<Boolean> info) {
        if (!excludeFromClaimAll && ((QuestAccessor) (Object) quest).isVillagerQuest()) {
            info.setReturnValue(true);
        }
    }

}
