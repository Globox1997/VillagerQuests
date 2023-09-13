package net.villagerquests.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;

public class QuestScreenHandler extends ScreenHandler {

    public final MerchantEntity offerer;
    public final List<Integer> questIdList = new ArrayList<>();

    public QuestScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(VillagerQuestsMain.QUEST_SCREEN_HANDLER_TYPE, syncId);
        this.offerer = ((MerchantAccessor) playerInventory.player).getCurrentOfferer();

        this.questIdList.clear();
        this.questIdList.addAll(((MerchantAccessor) offerer).getQuestIdList());

        // Remove quests if currently in use of other quest giver and if finished this particular quest while not show up when no refresh timer exist
        Iterator<Integer> iterator = this.questIdList.iterator();
        while (iterator.hasNext()) {
            int check = iterator.next();
            int questId = this.questIdList.get(this.questIdList.indexOf(check));
            if ((((PlayerAccessor) playerInventory.player).getPlayerQuestIdList().contains(questId) && !((PlayerAccessor) playerInventory.player).isOriginalQuestGiver(offerer.getUuid(), questId))
                    || (((PlayerAccessor) playerInventory.player).getPlayerFinishedQuestIdList().contains(questId) && ((PlayerAccessor) playerInventory.player).getPlayerQuestRefreshTimerList()
                            .get(((PlayerAccessor) playerInventory.player).getPlayerFinishedQuestIdList().indexOf(questId)).equals(-1))) {
                iterator.remove();
            }

        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.offerer.getCustomer() == player;
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index) {
        return ItemStack.EMPTY;
    }

}