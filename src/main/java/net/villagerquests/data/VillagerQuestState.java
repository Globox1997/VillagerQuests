package net.villagerquests.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
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

    public static VillagerQuestPlayerData getPlayerVillagerQuestState(MinecraftServer server, UUID playerUuid) {
        VillagerQuestState serverState = getServerVillagerQuestState(server);

        VillagerQuestPlayerData villagerQuestPlayerData = serverState.players.computeIfAbsent(playerUuid, uuid -> new VillagerQuestPlayerData());
        return villagerQuestPlayerData;
    }

    public static void updatePlayerVillagerQuestMarkType(MinecraftServer server, UUID playerUuid, UUID villagerUuid, int questMarkType) {
        getPlayerVillagerQuestState(server, playerUuid).getMerchantQuestMarkMap().put(villagerUuid, questMarkType);
    }

    public static void removeUuidFromServerVillagerQuestState(MinecraftServer server, UUID villagerUuid) {
        getServerVillagerQuestState(server).players.forEach((playerUuid, villagerQuestPlayerData) -> {
            villagerQuestPlayerData.getMerchantQuestMarkMap().remove(villagerUuid);
        });
    }

}
