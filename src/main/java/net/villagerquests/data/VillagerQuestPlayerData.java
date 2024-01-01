package net.villagerquests.data;

import java.util.HashMap;
import java.util.UUID;

public class VillagerQuestPlayerData {

    private HashMap<UUID, Integer> merchantQuestMarkMap = new HashMap<UUID, Integer>();

    public HashMap<UUID, Integer> getMerchantQuestMarkMap() {
        return this.merchantQuestMarkMap;
    }

}
