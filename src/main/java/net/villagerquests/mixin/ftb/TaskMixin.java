package net.villagerquests.mixin.ftb;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.MinecraftServer;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.data.VillagerQuestState;
import net.villagerquests.network.QuestServerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.UUID;

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

}
