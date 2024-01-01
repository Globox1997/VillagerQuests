package net.villagerquests.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class VillagerQuestState extends PersistentState {

    private HashMap<UUID, VillagerQuestPlayerData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, villagerQuestPlayerData) -> {

            NbtCompound merchantQuestMarkCompound = new NbtCompound();
            Iterator<Map.Entry<UUID, Integer>> iterator = villagerQuestPlayerData.getMerchantQuestMarkMap().entrySet().iterator();

            int count = 0;
            merchantQuestMarkCompound.putInt("MerchantQuestMarkCount", villagerQuestPlayerData.getMerchantQuestMarkMap().size());

            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                merchantQuestMarkCompound.putUuid("MerchantQuestMarkUuid" + count, entry.getKey());
                merchantQuestMarkCompound.putInt("MerchantQuestMarkId" + count, entry.getValue());
                count++;
            }
            nbt.put("MerchantQuestMark", merchantQuestMarkCompound);

            playersNbt.put(uuid.toString(), merchantQuestMarkCompound);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static VillagerQuestState createFromNbt(NbtCompound nbt) {
        VillagerQuestState villagerQuestState = new VillagerQuestState();

        NbtCompound playersNbt = nbt.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            VillagerQuestPlayerData playerData = new VillagerQuestPlayerData();

            NbtCompound merchantQuestMarkCompound = playersNbt.getCompound(key).getCompound("MerchantQuestMark");
            int count = merchantQuestMarkCompound.getInt("MerchantQuestMarkCount");

            for (int i = 0; i < count; i++) {
                playerData.getMerchantQuestMarkMap().put(merchantQuestMarkCompound.getUuid("MerchantQuestMarkUuid" + i), merchantQuestMarkCompound.getInt("MerchantQuestMarkId" + i));
            }

            UUID uuid = UUID.fromString(key);
            villagerQuestState.players.put(uuid, playerData);
        });

        return villagerQuestState;
    }

    public static VillagerQuestState getServerVillagerQuestState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        VillagerQuestState state = persistentStateManager.getOrCreate(VillagerQuestState::createFromNbt, VillagerQuestState::new, "villagerquest");
        state.markDirty();
        return state;
    }

    public static VillagerQuestPlayerData getPlayerVillagerQuestState(ServerPlayerEntity serverPlayerEntity) {
        VillagerQuestState serverState = getServerVillagerQuestState(serverPlayerEntity.getServer());

        VillagerQuestPlayerData villagerQuestPlayerData = serverState.players.computeIfAbsent(serverPlayerEntity.getUuid(), uuid -> new VillagerQuestPlayerData());
        return villagerQuestPlayerData;
    }

    public static void updatePlayerVillagerQuestMarkType(ServerPlayerEntity serverPlayerEntity, UUID uuid, int questMarkType) {
        getPlayerVillagerQuestState(serverPlayerEntity).getMerchantQuestMarkMap().put(uuid, questMarkType);
    }

    public static void removeUuidFromServerVillagerQuestState(MinecraftServer server, UUID uuid) {
        getServerVillagerQuestState(server).players.forEach((playerUuid, villagerQuestPlayerData) -> {
            villagerQuestPlayerData.getMerchantQuestMarkMap().remove(uuid);
        });
    }

}
