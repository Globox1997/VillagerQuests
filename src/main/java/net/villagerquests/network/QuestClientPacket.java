package net.villagerquests.network;

import java.util.Iterator;
import java.util.UUID;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.ftb.VillagerTalkTask;
import net.villagerquests.screen.VillagerQuestOpScreen;
import net.villagerquests.screen.VillagerQuestTalkScreen;

@Environment(EnvType.CLIENT)
public class QuestClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_QUEST_OFFERER, (client, handler, buf, sender) -> {
            int id = buf.readInt();
            client.execute(() -> {
                if (client.world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    merchantEntity.setCustomer(client.player);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_MERCHANT_QUEST_MARK, (client, handler, buf, sender) -> {
            int id = buf.readInt();
            int questMarkType = buf.readInt();
            client.execute(() -> {
                if (client.world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    ((MerchantAccessor) merchantEntity).setQuestMarkType(questMarkType);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.OP_MERCHANT_SCREEN_PACKET, (client, handler, buf, sender) -> {
            int id = buf.readInt();
            boolean defaultChangeableName = buf.readBoolean();
            boolean defaultInvincibility = buf.readBoolean();
            boolean defaultOffersTrades = buf.readBoolean();

            client.execute(() -> {
                if (client.player != null && client.player.isCreativeLevelTwoOp() && client.world != null && client.world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    client.setScreen(new VillagerQuestOpScreen(merchantEntity, defaultChangeableName, defaultInvincibility, defaultOffersTrades));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.OFFERS_TRADES, (client, handler, buf, sender) -> {
            int id = buf.readInt();
            boolean offersTrades = buf.readBoolean();
            client.execute(() -> {
                if (client.world.getEntityById(id) instanceof MerchantEntity merchantEntity) {
                    ((MerchantAccessor) merchantEntity).setOffersTrades(offersTrades);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.TALK, (client, handler, buf, sender) -> {
            int id = buf.readInt();
            long questId = buf.readLong();
            client.execute(() -> {
                if (client.world.getEntityById(id) instanceof MerchantEntity merchantEntity && ClientQuestFile.INSTANCE.get(questId) instanceof Quest quest) {
                    Iterator<Task> iterator = quest.getTasks().iterator();
                    TeamData teamData = ClientQuestFile.INSTANCE.selfTeamData;
                    while (iterator.hasNext()) {
                        Task task = iterator.next();
                        if (task instanceof VillagerTalkTask villagerTalkTask && teamData.getProgress(task) < task.getMaxProgress() && teamData.canStartTasks(task.getQuest())) {
                            client.setScreen(new VillagerQuestTalkScreen(merchantEntity, questId, villagerTalkTask.getTalkTextList().stream().map(TextUtils::parseRawText).toList()));
                        }
                    }
                }
            });
        });
    }

    public static void writeC2SScreenPacket(MerchantEntity merchantEntity, int mouseX, int mouseY, boolean villagerScreen) {
        ((MerchantAccessor) merchantEntity).setOffersTrades(true);

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntity.getId());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        buf.writeBoolean(villagerScreen);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.SET_SCREEN, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    // public static void writeC2SClosePacket(MerchantEntity merchantEntity) {
    // PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    // buf.writeInt(merchantEntity.getId());
    // CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.CLOSE_SCREEN, new PacketByteBuf(buf));
    // MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    // }

    public static void writeC2SAcceptQuestPacket(long questId, boolean acceptQuest) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(questId);
        buf.writeBoolean(acceptQuest);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.ACCEPT_QUEST, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SCompleteQuestPacket(long questId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(questId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.COMPLETE_QUEST, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SOpMerchantPacket(int merchantEntityId, String merchantName, boolean changeableName, boolean invincibility, boolean hasAi, boolean offersTrades) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntityId);
        buf.writeString(merchantName);
        buf.writeBoolean(changeableName);
        buf.writeBoolean(invincibility);
        buf.writeBoolean(hasAi);
        buf.writeBoolean(offersTrades);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.OP_MERCHANT_PACKET, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SUpdateMerchantQuestMark(UUID uuid) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(uuid);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.UPDATE_MERCHANT_QUEST_MARK, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2STalkPacket(int merchantEntityId, long questId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(merchantEntityId);
        buf.writeLong(questId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.COMPLETE_TALK, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }
}
