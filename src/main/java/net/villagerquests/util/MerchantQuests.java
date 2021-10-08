package net.villagerquests.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.VillagerData;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.data.QuestData;
import net.villagerquests.network.QuestServerPacket;

public class MerchantQuests {

    public static void addRandomMerchantQuests(MerchantEntity merchantEntity, int count) {
        if (!merchantEntity.world.isClient) {
            boolean isVillager = merchantEntity instanceof VillagerEntity;

            if (isVillager && VillagerQuestsMain.CONFIG.rememberQuests && ((MerchantAccessor) merchantEntity).getQuestIdList().isEmpty() && !((MerchantAccessor) merchantEntity).getJobList().isEmpty()
                    && ((MerchantAccessor) merchantEntity).getJobList().contains(((VillagerEntity) merchantEntity).getVillagerData().getProfession().toString())) {
                ((MerchantAccessor) merchantEntity).setQuestIdList(((MerchantAccessor) merchantEntity).getOldQuestList()
                        .get(((MerchantAccessor) merchantEntity).getJobList().indexOf(((VillagerEntity) merchantEntity).getVillagerData().getProfession().toString())));
            } else if (merchantEntity.world.random.nextFloat() > VillagerQuestsMain.CONFIG.noQuestChance) {
                List<Integer> availableQuests = new ArrayList<>();
                List<Integer> currentQuests = ((MerchantAccessor) merchantEntity).getQuestIdList();
                // Villager
                if (isVillager) {
                    VillagerData villagerData = ((VillagerEntity) merchantEntity).getVillagerData();
                    for (int i = 0; i < QuestData.idList.size(); i++) {
                        if (villagerData.getProfession().getId().equals(QuestData.professionList.get(i)) && villagerData.getLevel() >= QuestData.levelList.get(i)
                                && !currentQuests.contains(QuestData.idList.get(i)))
                            if (VillagerQuestsMain.CONFIG.canOnlyAddLevelSpecificQuests) {
                                if (villagerData.getLevel() == QuestData.levelList.get(i))
                                    availableQuests.add(QuestData.idList.get(i));
                            } else
                                availableQuests.add(QuestData.idList.get(i));
                    }
                } else {
                    // Wandering trader
                    for (int i = 0; i < QuestData.idList.size(); i++) {
                        if (QuestData.professionList.get(i).equals("wandering_trader"))
                            availableQuests.add(QuestData.idList.get(i));
                    }
                }
                count += (isVillager ? VillagerQuestsMain.CONFIG.villagerQuestExtraQuantity : VillagerQuestsMain.CONFIG.wanderingQuestQuantity - 2);
                if (availableQuests.size() > count) {
                    int questAdder = 0;
                    while (questAdder < count) {
                        int randomInt = merchantEntity.world.random.nextInt(availableQuests.size());
                        if (!currentQuests.contains(availableQuests.get(randomInt))) {
                            currentQuests.add(availableQuests.get(randomInt));
                            questAdder++;
                        }
                    }
                } else if (!availableQuests.isEmpty()) {
                    currentQuests.addAll(availableQuests);
                }
            }
            List<Integer> list = ((MerchantAccessor) merchantEntity).getQuestIdList();
            List<? extends PlayerEntity> playerList = merchantEntity.world.getPlayers();
            if (!list.isEmpty() && !playerList.isEmpty())
                for (int i = 0; i < playerList.size(); i++)
                    QuestServerPacket.writeS2CMerchantQuestsPacket((ServerPlayerEntity) playerList.get(i), merchantEntity, list);
        }
    }
}
