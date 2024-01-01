package net.villagerquests.mixin.ftb.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.RewardButton;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.villagerquests.access.QuestAccessor;

@Environment(EnvType.CLIENT)
@Mixin(RewardButton.class)
public abstract class RewardButtonMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    private Reward reward;

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void onClickedMixin(MouseButton button, CallbackInfo info) {
        if (((QuestAccessor) (Object) reward.getQuest()).isVillagerQuest()) {
            info.cancel();
        }
    }
}
