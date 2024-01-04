package net.villagerquests.mixin.ftb.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.access.TeamDataAccessor;
import net.villagerquests.util.QuestRenderHelper;

@Environment(EnvType.CLIENT)
@Mixin(QuestButton.class)
public class QuestButtonMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    Quest quest;

    @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
    public void addMouseOverText(TooltipList list, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && ((QuestAccessor) (Object) quest).getTimer() > 0) {
            if (!((TeamDataAccessor) TeamData.get(client.player)).getTimer().isEmpty() && ((TeamDataAccessor) TeamData.get(client.player)).getTimer().containsKey(quest.id)) {
                list.add(QuestRenderHelper.getTimerText(((QuestAccessor) (Object) quest).getTimer()
                        - Math.toIntExact(client.player.getWorld().getTime() - ((TeamDataAccessor) TeamData.get(client.player)).getTimer().get(quest.id))));
            } else {
                list.add(QuestRenderHelper.getTimerText(((QuestAccessor) (Object) quest).getTimer()));
            }
        }
    }

}
