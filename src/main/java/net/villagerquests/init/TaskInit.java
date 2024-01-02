package net.villagerquests.init;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.network.QuestServerPacket;
import net.villagerquests.util.QuestHelper;

@SuppressWarnings("unused")
public class TaskInit {

    public static TaskType VILLAGER_TALK = TaskTypes.register(new Identifier("ftbquests", "villager_talk"), VillagerTalkTask::new, () -> Icon.getIcon("minecraft:item/diamond_boots"));

    public static void init() {
        // ObjectCompletedEvent.QUEST.register((result) -> {
        //     if ((Object) result.getQuest() instanceof QuestAccessor questAccessor && questAccessor.isVillagerQuest()) {

        //         System.out.println("QUEST COMPLETED TaskInit Event Callback " + result.getQuest());

        //         for (int i = 0; i < result.getOnlineMembers().size(); i++) {
        //             ServerPlayerEntity serverPlayerEntity = result.getOnlineMembers().get(i);
        //             if (serverPlayerEntity.getServerWorld().getEntity(questAccessor.getVillagerQuestUuid()) instanceof MerchantEntity merchantEntity) {
        //                 int questMarkType = QuestHelper.getVillagerQuestMarkType(serverPlayerEntity, questAccessor.getVillagerQuestUuid());
        //                 QuestServerPacket.writeS2CMerchantQuestMarkPacket(serverPlayerEntity, merchantEntity.getId(), questMarkType);
        //             }
        //         }
        //     }
        //     return EventResult.pass();
        // });
    }

}
