package net.villagerquests.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.Identifier;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;

public class QuestScreenHandler extends SyncedGuiDescription {

    public static final Identifier GUI_ICONS = new Identifier("villagerquests:textures/gui/screen_and_icons.png");
    public final MerchantScreenHandler merchantScreenHandler;
    public final MerchantEntity offerer = ((MerchantAccessor) playerInventory.player).getCurrentOfferer();
    public final List<Integer> questIdList = new ArrayList<>();

    public QuestScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, MerchantScreenHandler merchantScreenHandler) {
        super(VillagerQuestsMain.QUEST_SCREEN_HANDLER_TYPE, syncId, playerInventory);
        this.merchantScreenHandler = merchantScreenHandler;

        this.questIdList.clear();
        this.questIdList.addAll(((MerchantAccessor) offerer).getQuestIdList());

        // Remove quests if currently in use of other quest giver and if finished this particular quest
        Iterator<Integer> iterator = this.questIdList.iterator();
        while (iterator.hasNext()) {
            int check = iterator.next();
            if ((((PlayerAccessor) playerInventory.player).getPlayerQuestIdList().contains(this.questIdList.get(this.questIdList.indexOf(check)))
                    && !((PlayerAccessor) playerInventory.player).isOriginalQuestGiver(offerer.getUuid(), this.questIdList.get(this.questIdList.indexOf(check))))
                    || ((PlayerAccessor) playerInventory.player).getFinishedPlayerQuestIdList().contains(this.questIdList.get(this.questIdList.indexOf(check))))
                iterator.remove();
        }

        System.out.println("LOL:" + this.questIdList);

        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(276, 166);
        root.setInsets(Insets.ROOT_PANEL);

        root.validate(this);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.offerer.getCurrentCustomer() == player;
    }

}