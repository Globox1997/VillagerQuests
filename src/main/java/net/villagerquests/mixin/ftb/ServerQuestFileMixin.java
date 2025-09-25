package net.villagerquests.mixin.ftb;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@SuppressWarnings("rawtypes")
@Mixin(ServerQuestFile.class)
public class ServerQuestFileMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    public MinecraftServer server;

    // Maybe this has to get removed.
    // When player is offline and other team members finish the quest
    @Redirect(method = "lambda$checkQuestBookOnLogin$4", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/Quest;onCompleted(Ldev/ftb/mods/ftbquests/events/QuestProgressEventData;)V"), remap = false)
    private static void playerLoggedInMixin(Quest quest, QuestProgressEventData questProgressEventData, TeamData data, Date date, Collection<ServerPlayerEntity> collection, List<ServerPlayerEntity> list, ServerPlayerEntity player, Quest iteratingQuest) {
        if (!((QuestAccessor) (Object) iteratingQuest).isVillagerQuest()) {
            quest.onCompleted(new QuestProgressEventData(date, data, quest, collection, list));
        }
    }

    @Inject(method = "deleteObject", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/QuestObjectBase;deleteChildren()V"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void deleteObjectMixin(long id, CallbackInfo info, QuestObjectBase object) {
        if (object instanceof Quest quest && ((QuestAccessor) (Object) quest).isVillagerQuest() && ((QuestAccessor) (Object) quest).getVillagerQuestUuid() != null) {
            UUID villagerUuid = ((QuestAccessor) (Object) quest).getVillagerQuestUuid();
            Iterator<UUID> playerUuids = VillagerQuestState.getPlayerVillagerQuestState(server, villagerUuid).getMerchantQuestMarkMap().keySet().iterator();
            while (playerUuids.hasNext()) {
                UUID playerUuid = playerUuids.next();
                int questMarkType = -1;
                if (server.getPlayerManager().getPlayer(playerUuid) != null) {
                    questMarkType = QuestHelper.getVillagerQuestMarkType(server.getPlayerManager().getPlayer(playerUuid), villagerUuid);
                    if (server.getPlayerManager().getPlayer(playerUuid).getServerWorld().getEntity(villagerUuid) != null) {
                        QuestServerPacket.writeS2CMerchantQuestMarkPacket(server.getPlayerManager().getPlayer(playerUuid),
                                server.getPlayerManager().getPlayer(playerUuid).getServerWorld().getEntity(villagerUuid).getId(), questMarkType);
                    }
                }
                VillagerQuestState.updatePlayerVillagerQuestMarkType(server, playerUuid, villagerUuid, questMarkType);
            }
        }
    }
}
