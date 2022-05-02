package net.villagerquests.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.MouseAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.QuestData;
import net.villagerquests.data.QuestLoader;

public class QuestClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_QUEST_OFFERER, (client, handler, buf, sender) -> {
            PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
            newBuffer.writeVarInt(buf.readVarInt());
            newBuffer.writeIntList(buf.readIntList());
            client.execute(() -> {
                MerchantEntity merchantEntity = (MerchantEntity) client.world.getEntityById(newBuffer.readVarInt());
                ((MerchantAccessor) merchantEntity).setQuestIdList(newBuffer.readIntList());
                ((MerchantAccessor) client.player).setCurrentOfferer(merchantEntity);
                merchantEntity.setCustomer(client.player);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.QUEST_KILL_ADDITION, (client, handler, buf, sender) -> {
            if (client.player != null) {
                ((PlayerAccessor) client.player).canAddKilledMobQuestCount(buf.readInt());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_MOUSE_POSITION, (client, handler, buf, sender) -> {
            int mouseX = buf.readInt();
            int mouseY = buf.readInt();
            client.execute(() -> {
                ((MouseAccessor) client.mouse).setMousePosition(mouseX, mouseY);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SYNC_PLAYER_QUEST_DATA, (client, handler, buf, sender) -> {
            if (client.player != null)
                executePlayerQuestData(client.player, buf);
            else {
                PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));

                int UuidSize = buf.readInt();
                newBuffer.writeInt(UuidSize);
                for (int i = 0; i < UuidSize; i++) {
                    newBuffer.writeUuid(buf.readUuid());
                }

                int killedMobCountSize = buf.readInt();
                newBuffer.writeInt(killedMobCountSize);
                for (int u = 0; u < killedMobCountSize; u++) {
                    newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                }

                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));
                newBuffer.writeIntList(new IntArrayList(buf.readIntList()));

                while (buf.isReadable()) {
                    newBuffer.writeString(buf.readString());
                }
                client.execute(() -> {
                    executePlayerQuestData(client.player, newBuffer);
                });
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.FAIL_MERCHANT_QUEST, (client, handler, buf, sender) -> {
            PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
            newBuffer.writeInt(buf.readInt());
            newBuffer.writeInt(buf.readInt());
            client.execute(() -> {
                ((PlayerAccessor) client.player).failPlayerQuest(newBuffer.readInt(), newBuffer.readInt());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.REMOVE_MERCHANT_QUEST, (client, handler, buf, sender) -> {
            int questId = buf.readInt();
            client.execute(() -> {
                ((PlayerAccessor) client.player).removeQuest(questId);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_MOUSE_POSITION, (client, handler, buf, sender) -> {
            int mouseX = buf.readInt();
            int mouseY = buf.readInt();
            client.execute(() -> {
                ((MouseAccessor) client.mouse).setMousePosition(mouseX, mouseY);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.QUEST_LIST_DATA, (client, handler, buf, sender) -> {
            if (client.player != null) {
                executeListPacket(buf, client.player);
            } else {
                PacketByteBuf newBuffer = new PacketByteBuf(Unpooled.buffer());
                while (buf.isReadable()) {
                    newBuffer.writeString(buf.readString());
                }
                client.execute(() -> {
                    executeListPacket(newBuffer, client.player);
                });
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.SET_MERCHANT_QUEST, (client, handler, buf, sender) -> {
            int entityId = buf.readVarInt();
            List<Integer> list = buf.readIntList();
            client.execute(() -> {
                ((MerchantAccessor) client.world.getEntityById(entityId)).setQuestIdList(list);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(QuestServerPacket.QUEST_TRAVEL_ADDITION, (client, handler, buf, sender) -> {
            if (client.player != null) {
                ((PlayerAccessor) client.player).getPlayerTravelList().get(buf.readInt()).set(buf.readInt(), true);
            }
        });
    }

    public static void writeC2SScreenPacket(MerchantEntity merchantEntity, int mouseX, int mouseY) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.SET_QUEST_SCREEN, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void acceptMerchantQuestC2SPacket(PlayerEntity playerEntity, int questId, UUID uuid, int entityId) {
        // Set on client
        ((PlayerAccessor) playerEntity).addPlayerQuestId(questId, uuid);

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        buf.writeUuid(uuid);
        buf.writeInt(entityId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.ACCEPT_MERCHANT_QUEST, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2STradePacket(MerchantEntity merchantEntity, int mouseX, int mouseY) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        buf.writeInt(mouseX);
        buf.writeInt(mouseY);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.SET_TRADE_SCREEN, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SClosePacket(MerchantEntity merchantEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(merchantEntity.getId());
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.CLOSE_SCREEN, new PacketByteBuf(buf));
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SQuestCompletionPacket(int questId, int entityId, int questLevel) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        buf.writeInt(entityId);
        buf.writeInt(questLevel);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.COMPLETE_MERCHANT_QUEST, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SQuestDeclinePacket(int questId, int reason, int entityId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(questId);
        buf.writeInt(reason);
        buf.writeInt(entityId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(QuestServerPacket.DECLINE_MERCHANT_QUEST, buf);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    private static void executePlayerQuestData(PlayerEntity player, PacketByteBuf buf) {
        List<Integer> questIds = buf.readIntList();

        int UuidSize = buf.readInt();
        List<UUID> traderUuids = new ArrayList<>();
        for (int i = 0; i < UuidSize; i++) {
            traderUuids.add(buf.readUuid());
        }

        int killedMobCountSize = buf.readInt();
        List<List<Integer>> killedMobQuestCount = new ArrayList<>();
        for (int u = 0; u < killedMobCountSize; u++) {
            killedMobQuestCount.add(buf.readIntList());
        }

        List<Integer> finishedIds = buf.readIntList();
        List<Integer> timers = buf.readIntList();
        List<Integer> refreshTimers = buf.readIntList();

        List<List<Object>> travelIdList = new ArrayList<>();

        List<Object> travelCollectorList = new ArrayList<>();
        while (buf.isReadable()) {
            String string = buf.readString();
            if (string.equals("Null")) {
                travelCollectorList.clear();
                travelIdList.add(List.copyOf(travelCollectorList));
            } else if (string.equals("Break")) {
                travelIdList.add(List.copyOf(travelCollectorList));
                travelCollectorList.clear();
            } else {
                if (string.equals("true"))
                    travelCollectorList.add(true);
                else if (string.equals("false"))
                    travelCollectorList.add(false);
                else
                    travelCollectorList.add(string);
            }
        }
        ((PlayerAccessor) player).syncPlayerQuest(questIds, killedMobQuestCount, travelIdList, traderUuids, finishedIds, timers, refreshTimers);
    }

    private static void executeListPacket(PacketByteBuf buf, ClientPlayerEntity player) {
        QuestLoader.clearEveryList();
        ArrayList<String> list = new ArrayList<>();
        while (buf.isReadable()) {
            list.add(buf.readString());
        }
        for (int i = 0; i < list.size(); i++) {
            String listName = list.get(i).toString();
            if (listName.equals("questTaskList") || listName.equals("questRewardList")) {
                List<Object> newList = new ArrayList<>();
                for (int u = i + 1; u < list.size(); u++) {
                    String object = list.get(u).toString();
                    if (QuestData.getList(object) != null)
                        break;
                    else if (object.equals("stop")) {
                        addToList(listName, null, newList);
                        newList.clear();
                    } else if (object.matches("-?(0|[1-9]\\d*)")) {
                        newList.add(Integer.parseInt(object));
                    } else
                        newList.add(object);
                }

            } else if (QuestData.getList(listName) != null) {
                for (int u = i + 1; u < list.size(); u++) {
                    String object = list.get(u).toString();
                    if (QuestData.getList(object) == null)
                        addToList(listName, object, null);
                    else
                        break;
                }
            }
        }
    }

    private static void addToList(String listName, String object, List<Object> list) {
        if (list != null) {
            QuestData.getList(listName).add(new ArrayList<Object>(list));

        } else if (object.matches("-?(0|[1-9]\\d*)")) {
            QuestData.getList(listName).add(Integer.parseInt(object));
        } else
            QuestData.getList(listName).add(object);
    }

}
