package net.villagerquests.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.villagerquests.accessor.PlayerAccessor;

public class Quest {
    private final int id;
    private final String title;
    private final String type;
    private final String description;
    private final int experience;
    private final int timer;
    private final int refreshTime;
    private final List<Object> taskList = new ArrayList<>();
    private final List<Object> rewardList = new ArrayList<>();

    private static final Logger LOGGER = LogManager.getLogger("VillagerQuests");

    public Quest(int id) {
        this.id = id;
        int index = QuestData.idList.indexOf(id);
        this.title = QuestData.titleList.get(index);
        this.type = QuestData.typeList.get(index);
        this.description = QuestData.descriptionList.get(index);
        this.experience = QuestData.experienceList.get(index);
        this.taskList.addAll(QuestData.taskList.get(index));
        this.rewardList.addAll(QuestData.rewardList.get(index));
        this.timer = QuestData.timerList.get(index);
        this.refreshTime = QuestData.refreshTimeList.get(index);
    }

    public ItemStack getQuestTypeStack() {
        switch (type) {
            case "fight":
                return new ItemStack(Items.IRON_SWORD);
            case "farm":
                return new ItemStack(Items.IRON_HOE);
            case "mine":
                return new ItemStack(Items.IRON_PICKAXE);
            default:
                return ItemStack.EMPTY;
        }
    }

    public int getQuestId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getExperienceAmount() {
        return experience;
    }

    public String[] getStringTasks() {
        String[] taskListString = new String[this.taskList.size() / 3];
        try {
            for (int i = 0; i < this.taskList.size() / 3; i++) {
                String task = (String) this.taskList.get(i * 3);
                int count = (int) this.taskList.get(i * 3 + 2);
                taskListString[i] = "Task " + i + " - " + WordUtils.capitalize(task) + " " + count + " " + getTranslatedRegistryName(task, (String) this.taskList.get(i * 3 + 1))
                        + (count > 1 ? "s" : "");
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while loading quest tasks {}. {}", this.title, e.toString());
        }

        return taskListString;
    }

    public String[] getStringRewards() {
        boolean rewardsExperience = getExperienceAmount() > 0;
        String[] taskListString = new String[this.rewardList.size() / 2 + (rewardsExperience ? 1 : 0)];
        if (rewardsExperience) {
            taskListString[0] = getExperienceAmount() + " Experience";
        }
        try {
            for (int i = 0; i < this.rewardList.size() / 2; i++) {
                int count = (int) this.rewardList.get(i * 2 + 1);
                taskListString[i + (rewardsExperience ? 1 : 0)] = count + " " + getTranslatedRegistryName("submit", (String) this.rewardList.get(i * 2)) + (count > 1 ? "s" : "");
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while loading quest rewards {}. {}", this.title, e.toString());
        }

        return taskListString;
    }

    public int getQuestTimer() {
        return timer;
    }

    public int getQuestRefreshTimer() {
        return refreshTime;
    }

    public ItemStack getTaskStack(int index) {
        String string = (String) this.taskList.get(index * 3 + 1);
        if (((String) this.taskList.get(index * 3)).equals("kill"))
            return ItemStack.EMPTY;
        else
            return new ItemStack(Registry.ITEM.get(new Identifier(string)));
    }

    public ItemStack getRewardStack(int index) {
        boolean rewardsExperience = getExperienceAmount() > 0;
        if (index == 0 && rewardsExperience)
            return new ItemStack(Items.EXPERIENCE_BOTTLE);
        String string = (String) this.rewardList.get(rewardsExperience ? (index - 1) * 2 : index * 2);
        return new ItemStack(Registry.ITEM.get(new Identifier(string)));
    }

    private String getTranslatedRegistryName(String task, String identifier) {
        switch (task) {
            case "kill":
                return Registry.ENTITY_TYPE.get(new Identifier(identifier)).getName().getString();
            case "farm":
                return Registry.ITEM.get(new Identifier(identifier)).getName().getString();
            case "submit":
                return Registry.ITEM.get(new Identifier(identifier)).getName().getString();
            case "mine":
                return Registry.BLOCK.get(new Identifier(identifier)).getName().getString();
            default:
                return "";
        }
    }

    public void getRewards(PlayerEntity playerEntity) {
        playerEntity.addExperience(getExperienceAmount());
        for (int i = 0; i < this.rewardList.size() / 2; i++) {
            ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier((String) this.rewardList.get(i * 2))), (int) this.rewardList.get(i * 2 + 1));
            playerEntity.getInventory().offerOrDrop(stack);
        }
    }

    public void consumeCompletedQuestItems(PlayerEntity playerEntity) {
        for (int i = 0; i < this.taskList.size() / 3; i++) {
            if (!this.taskList.get(i * 3).equals("kill")) {

                int deleteAmount = (int) this.taskList.get(i * 3 + 2);
                for (int u = 0; u < playerEntity.getInventory().size(); u++) {
                    if (playerEntity.getInventory().getStack(u).isItemEqualIgnoreDamage(new ItemStack(Registry.ITEM.get(new Identifier((String) this.taskList.get(i * 3 + 1)))))) {
                        if (deleteAmount < playerEntity.getInventory().getStack(u).getCount()) {
                            playerEntity.getInventory().getStack(u).decrement(deleteAmount);
                        } else {
                            deleteAmount -= playerEntity.getInventory().getStack(u).getCount();
                            playerEntity.getInventory().getStack(u).decrement(playerEntity.getInventory().getStack(u).getCount());
                        }
                    }
                    if (deleteAmount <= 0)
                        break;

                }
            }
        }
    }

    public boolean canCompleteQuest(PlayerEntity playerEntity) {
        for (int i = 0; i < this.taskList.size() / 3; i++) {
            if (this.taskList.get(i * 3).equals("kill")) {

                int index = ((PlayerAccessor) playerEntity).getPlayerQuestIdList().indexOf(this.id);
                List<List<Integer>> countList = ((PlayerAccessor) playerEntity).getPlayerKilledQuestList();

                if (countList.isEmpty())
                    continue;

                for (int u = 0; u < countList.get(index).size() / 2; u++) {
                    if (Registry.ENTITY_TYPE.getRawId(Registry.ENTITY_TYPE.get(new Identifier((String) this.taskList.get(i * 3 + 1)))) == (int) countList.get(index).get(u * 2)) {
                        if (countList.get(index).get(u * 2 + 1) < (int) this.taskList.get(i * 3 + 2)) {
                            return false;
                        }
                    }
                }
            } else {
                int itemCount = 0;
                for (int k = 0; k < playerEntity.getInventory().size(); k++) {
                    if (playerEntity.getInventory().getStack(k).isItemEqualIgnoreDamage(new ItemStack(Registry.ITEM.get(new Identifier((String) this.taskList.get(i * 3 + 1))))))
                        itemCount += playerEntity.getInventory().getStack(k).getCount();
                }
                if (itemCount == 0 || itemCount < (int) this.taskList.get(i * 3 + 2)) {
                    return false;
                }

            }
        }
        return true;
    }

    public List<Integer> getKillTaskEntityIds() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < this.taskList.size() / 3; i++) {
            if (this.taskList.get(i * 3).equals("kill")) {
                list.add(Registry.ENTITY_TYPE.getRawId(Registry.ENTITY_TYPE.get(new Identifier((String) this.taskList.get(i * 3 + 1)))));
                list.add(0);
            }
        }
        return list;
    }

    public static Quest getQuestById(int questId) {
        return new Quest(questId);
    }

    public static void failMerchantQuest(MerchantEntity merchantEntity) {
        if (merchantEntity.world instanceof ServerWorld) {
            Iterator<ServerPlayerEntity> var2 = ((ServerWorld) merchantEntity.world).getServer().getPlayerManager().getPlayerList().iterator();
            while (var2.hasNext()) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var2.next();
                if (((PlayerAccessor) serverPlayerEntity).getPlayerQuestTraderIdList().contains(merchantEntity.getUuid())) {
                    ((PlayerAccessor) serverPlayerEntity).failPlayerQuest(
                            ((PlayerAccessor) serverPlayerEntity).getPlayerQuestIdList().get(((PlayerAccessor) serverPlayerEntity).getPlayerQuestTraderIdList().indexOf(merchantEntity.getUuid())));
                }
            }
        }
    }

}
