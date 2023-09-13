package net.villagerquests.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.network.QuestServerPacket;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;

public class QuestCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment.dedicated) {
                dispatcher.register((CommandManager.literal("quest").requires((serverCommandSource) -> {
                    return serverCommandSource.hasPermissionLevel(3);
                })).then((CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("removeAll").executes((commandContext) -> {
                    return executeQuestCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), 0, true);
                })).then(CommandManager.literal("remove").then(CommandManager.argument("id", IntegerArgumentType.integer()).executes((commandContext) -> {
                    return executeQuestCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "id"), false);
                }))))));
            }
        });
    }

    private static int executeQuestCommand(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int questId, boolean removeAll) {
        Iterator<ServerPlayerEntity> var3 = targets.iterator();
        while (var3.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var3.next();
            List<Integer> list = ((PlayerAccessor) serverPlayerEntity).getPlayerQuestIdList();
            if (list.isEmpty())
                continue;
            if (removeAll) {
                for (int i = 0; i < list.size(); i++) {
                    QuestServerPacket.writeS2CRemoveQuestPacket(serverPlayerEntity, list.get(i));
                    ((PlayerAccessor) serverPlayerEntity).removeQuest(list.get(i));
                }
            } else {
                if (list.contains(questId)) {
                    QuestServerPacket.writeS2CRemoveQuestPacket(serverPlayerEntity, questId);
                    ((PlayerAccessor) serverPlayerEntity).removeQuest(questId);
                } else {
                    source.sendFeedback(() -> Text.translatable("commands.villagerquests.changeFail", serverPlayerEntity.getName()), true);
                }
            }
        }
        source.sendFeedback(() -> Text.translatable("commands.villagerquests.changed"), true);

        return targets.size();
    }

}