package net.villagerquests.mixin.ftb;

import java.util.Collections;
import java.util.Date;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;

@SuppressWarnings("rawtypes")
@Mixin(ServerQuestFile.class)
public class ServerQuestFileMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    public MinecraftServer server;

    @Redirect(method = "lambda$playerLoggedIn$4", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/Quest;onCompleted(Ldev/ftb/mods/ftbquests/events/QuestProgressEventData;)V"), remap = false)
    private static void playerLoggedInMixin(Quest quest, QuestProgressEventData questProgressEventData, TeamData data, ServerPlayerEntity player, Quest iteratingQuest) {
        if (!((QuestAccessor) (Object) iteratingQuest).isVillagerQuest()) {
            quest.onCompleted(new QuestProgressEventData<>(new Date(), data, iteratingQuest, data.getOnlineMembers(), Collections.singletonList(player)));
        }
    }

    @Inject(method = "deleteObject", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/QuestObjectBase;deleteChildren()V"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void deleteObjectMixin(long id, CallbackInfo info, QuestObjectBase object) {
        if (object instanceof Quest quest && ((QuestAccessor) (Object) quest).isVillagerQuest()) {
            VillagerQuestState.removeUuidFromServerVillagerQuestState(server, ((QuestAccessor) (Object) quest).getVillagerQuestUuid());
        }
    }
}
