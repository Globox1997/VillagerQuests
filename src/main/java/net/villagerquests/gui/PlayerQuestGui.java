package net.villagerquests.gui;

import java.util.List;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WDynamicLabel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;

// UV = x / width

public class PlayerQuestGui extends LightweightGuiDescription {

    public PlayerQuestGui(MinecraftClient client) {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(300, 215);

        root.add(new WLabel(new TranslatableText("text.villagerquests.info")).setColor(VillagerQuestsMain.CONFIG.playerInfoColor), 6, 7);

        PlayerQuestInnerPanel plainPanel = new PlayerQuestInnerPanel();

        WScrollPanel scrollPanel = new WScrollPanel(plainPanel);
        scrollPanel.setScrollingHorizontally(TriState.FALSE);

        int gridYSpace = 0;
        List<Integer> questIdList = ((PlayerAccessor) client.player).getPlayerQuestIdList();
        List<Integer> questFinishedList = ((PlayerAccessor) client.player).getPlayerFinishedQuestIdList();
        List<List<Integer>> questKilledList = ((PlayerAccessor) client.player).getPlayerKilledQuestList();
        List<Integer> questRefreshTimerList = ((PlayerAccessor) client.player).getPlayerQuestRefreshTimerList();
        List<Integer> questTimerList = ((PlayerAccessor) client.player).getPlayerQuestTimerList();
        List<List<Object>> questTraveledList = ((PlayerAccessor) client.player).getPlayerTravelList();
        // List<UUID> questTraderList = ((PlayerAccessor) client.player).getPlayerQuestTraderIdList();
        if (!questIdList.isEmpty()) {
            plainPanel.add(new WLabel(new TranslatableText("text.villagerquests.active")).setColor(VillagerQuestsMain.CONFIG.playerActiveColor), 0, gridYSpace);
            gridYSpace += 22;

            for (int i = 0; i < questIdList.size(); i++) {
                int topBackGround = gridYSpace + 7;
                AddTopBackground(gridYSpace, plainPanel);

                Quest quest = Quest.getQuestById(questIdList.get(i));

                // Title
                plainPanel.add(new WLabel(Text.of(quest.getTitle())).setColor(VillagerQuestsMain.CONFIG.playerTitleColor), 3, gridYSpace);
                // Timer
                if (quest.getQuestTimer() != -1) {
                    int lambdaInt = i;
                    WDynamicLabel timeLabel = new WDynamicLabel(() -> {
                        if (questTimerList.size() > lambdaInt) {
                            int seconds = questTimerList.get(lambdaInt) / 20;
                            String string;
                            if (seconds >= 3600)
                                string = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
                            else
                                string = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
                            return new TranslatableText("text.villagerquests.timer").getString() + string;
                        } else
                            return new TranslatableText("text.villagerquests.timer").getString() + "00:00";
                    });
                    plainPanel.add(timeLabel.setColor(VillagerQuestsMain.CONFIG.playerTaskHeaderColor, VillagerQuestsMain.CONFIG.playerTaskHeaderColor), 225 - timeLabel.getWidth(), gridYSpace);
                }

                gridYSpace += 20;
                plainPanel.add(new WLabel(new TranslatableText("text.villagerquests.description")).setColor(VillagerQuestsMain.CONFIG.playerDescriptionHeaderColor), 3, gridYSpace);
                gridYSpace += 14;

                // Description
                int descriptioWidth = client.textRenderer.getWidth(quest.getDescription());
                if (descriptioWidth > 276) {
                    String[] string = quest.getDescription().split(" ");
                    String stringCollector = "";

                    for (int u = 0; u < string.length; u++) {
                        if (client.textRenderer.getWidth(stringCollector) < 250 && client.textRenderer.getWidth(stringCollector) + client.textRenderer.getWidth(string[u]) <= 250) {
                            stringCollector = stringCollector + " " + string[u];
                            if (u == string.length - 1) {
                                plainPanel.add(new WLabel(Text.of(stringCollector)).setColor(VillagerQuestsMain.CONFIG.playerDescriptionColor), 9, gridYSpace);
                            }
                        } else {
                            plainPanel.add(new WLabel(Text.of(stringCollector)).setColor(VillagerQuestsMain.CONFIG.playerDescriptionColor), 9, gridYSpace);
                            gridYSpace += 10;
                            stringCollector = string[u];
                            if (u == string.length - 1)
                                plainPanel.add(new WLabel(Text.of(stringCollector)).setColor(VillagerQuestsMain.CONFIG.playerDescriptionColor), 9, gridYSpace);

                        }
                    }
                } else
                    plainPanel.add(new WLabel(Text.of(quest.getDescription())).setColor(VillagerQuestsMain.CONFIG.playerDescriptionColor), 9, gridYSpace);

                gridYSpace += 17;

                plainPanel.add(new WLabel(new TranslatableText("text.villagerquests.tasks")).setColor(VillagerQuestsMain.CONFIG.playerTaskHeaderColor), 3, gridYSpace);
                gridYSpace += 14;

                // Tasks
                int easyKilledCounter = 0;
                int easyTravelCounter = 0;
                for (int u = 0; u < quest.getStringTasks().length; u++) {
                    if (quest.getStringTasks()[u].contains("Kill")) {
                        String string = quest.getStringTasks()[u] + " - " + new TranslatableText("text.villagerquests.killed").getString()
                                + questKilledList.get(questIdList.indexOf(quest.getQuestId())).get(easyKilledCounter * 2 + 1) + " / " + quest.getTaskCount(u);
                        if (client.textRenderer.getWidth(string) > 250) {
                            string = quest.getStringTasks()[u];
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 9, gridYSpace);
                            gridYSpace += 10;
                            string = new TranslatableText("text.villagerquests.killed").getString() + questKilledList.get(questIdList.indexOf(quest.getQuestId())).get(easyKilledCounter * 2 + 1)
                                    + " / " + quest.getTaskCount(u);
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 56, gridYSpace);
                        } else
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 9, gridYSpace);
                        easyKilledCounter++;
                    } else if (quest.getStringTasks()[u].contains("Explore") || quest.getStringTasks()[u].contains("Travel")) {
                        boolean traveled = (boolean) questTraveledList.get(questIdList.indexOf(quest.getQuestId())).get(easyTravelCounter * 2 + 1);
                        String string = quest.getStringTasks()[u] + " - " + new TranslatableText("text.villagerquests.explored").getString() + (traveled ? "Yes" : "Not Yet");
                        if (client.textRenderer.getWidth(string) > 250) {
                            string = quest.getStringTasks()[u];
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 9, gridYSpace);
                            gridYSpace += 10;
                            string = new TranslatableText("text.villagerquests.explored").getString() + (traveled ? "Yes" : "Not Yet");
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 56, gridYSpace);
                        } else
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 9, gridYSpace);
                        easyTravelCounter++;
                    } else {
                        int itemCount = 0;
                        for (int k = 0; k < client.player.getInventory().size(); k++) {
                            if (client.player.getInventory().getStack(k).isItemEqualIgnoreDamage(quest.getTaskStack(u)))
                                itemCount += client.player.getInventory().getStack(k).getCount();
                        }
                        String string = quest.getStringTasks()[u] + " - " + new TranslatableText("text.villagerquests.collected").getString() + itemCount + " / " + quest.getTaskCount(u);
                        if (client.textRenderer.getWidth(string) > 250) {
                            string = quest.getStringTasks()[u];
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 9, gridYSpace);
                            gridYSpace += 10;
                            string = new TranslatableText("text.villagerquests.collected").getString() + itemCount + " / " + quest.getTaskCount(u);
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 56, gridYSpace);
                        } else
                            plainPanel.add(new WLabel(Text.of(string)).setColor(VillagerQuestsMain.CONFIG.playerTaskColor), 9, gridYSpace);
                    }

                    gridYSpace += 10;
                }
                gridYSpace += 7;
                plainPanel.add(new WLabel(new TranslatableText("text.villagerquests.rewards")).setColor(VillagerQuestsMain.CONFIG.playerRewardHeaderColor), 3, gridYSpace);
                gridYSpace += 14;

                // Rewards
                for (int u = 0; u < quest.getStringRewards().length; u++) {
                    plainPanel.add(new WLabel(Text.of(quest.getStringRewards()[u])).setColor(VillagerQuestsMain.CONFIG.playerRewardColor), 9, gridYSpace);
                    if (u != quest.getStringRewards().length - 1)
                        gridYSpace += 10;
                    else
                        gridYSpace += 3;
                }
                AddMidBottomBackground(topBackGround, gridYSpace, plainPanel);

                gridYSpace += 20;
            }
        }

        boolean isRefreshListEmpty = true;
        int refreshListCount = 0;
        for (int i = 0; i < questRefreshTimerList.size(); i++) {
            if (questRefreshTimerList.get(i) != -1) {
                isRefreshListEmpty = false;
                refreshListCount++;
            }
        }
        if (!isRefreshListEmpty) {
            plainPanel.add(new WLabel(new TranslatableText("text.villagerquests.refresh")).setColor(VillagerQuestsMain.CONFIG.playerRefreshHeaderColor), 0, gridYSpace);
            gridYSpace += 22;

            int topBackGround = gridYSpace + 7;
            AddTopBackground(gridYSpace, plainPanel);

            for (int i = 0; i < questRefreshTimerList.size(); i++) {
                if (questRefreshTimerList.get(i) != -1) {

                    Quest quest = Quest.getQuestById(questFinishedList.get(i));

                    // Title
                    plainPanel.add(new WLabel(Text.of(quest.getTitle())), 3, gridYSpace);
                    gridYSpace += 16;

                    int lambdaInt = i;
                    WDynamicLabel timeLabel = new WDynamicLabel(() -> {
                        if (questRefreshTimerList.size() > lambdaInt) {
                            int seconds = questRefreshTimerList.get(lambdaInt) / 20;
                            String string;
                            if (seconds >= 3600)
                                string = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
                            else
                                string = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
                            return new TranslatableText("text.villagerquests.refreshing").getString() + string;
                        } else
                            return new TranslatableText("text.villagerquests.refreshing").getString() + "00:00";
                    });

                    plainPanel.add(timeLabel.setColor(VillagerQuestsMain.CONFIG.playerRefreshColor, VillagerQuestsMain.CONFIG.playerRefreshColor), 9, gridYSpace);
                    gridYSpace += 20;
                }
            }
            gridYSpace -= 17;
            AddMidBottomBackground(topBackGround, gridYSpace, plainPanel);
            gridYSpace += 20;
        }

        if (!questFinishedList.isEmpty() && questFinishedList.size() != refreshListCount) {
            plainPanel.add(new WLabel(new TranslatableText("text.villagerquests.finished")).setColor(VillagerQuestsMain.CONFIG.playerFinishedHeaderColor), 0, gridYSpace);
            gridYSpace += 22;
            int topBackGround = gridYSpace + 7;
            AddTopBackground(gridYSpace, plainPanel);

            for (int i = 0; i < questFinishedList.size(); i++) {
                if (questRefreshTimerList.get(i) == -1) {
                    Quest quest = Quest.getQuestById(questFinishedList.get(i));

                    // Title
                    plainPanel.add(new WLabel(Text.of(quest.getTitle())).setColor(VillagerQuestsMain.CONFIG.playerFinishedColor), 3, gridYSpace);
                    gridYSpace += 20;
                }
            }
            gridYSpace -= 17;
            AddMidBottomBackground(topBackGround, gridYSpace, plainPanel);
            gridYSpace += 10;
        }

        if (gridYSpace <= 180) {
            scrollPanel.setScrollingVertically(TriState.FALSE);
        }
        plainPanel.setSize(300, gridYSpace);

        root.add(scrollPanel, 10, 20, 280, 185);
        root.validate(this);
    }

    // @Override
    // public void addPainters() {
    // if (this.rootPanel != null && !fullscreen) {
    // this.rootPanel.setBackgroundPainter(BackgroundPainter.createLightDarkVariants(BackgroundPainter.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_light.png")),
    // BackgroundPainter.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_dark.png"))));
    // }
    // }

    private void AddTopBackground(int gridYSpace, PlayerQuestInnerPanel plainPanel) {
        // Top Left
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.0F, 0.59375F, 0.01953125F, 0.61328125F), 0, gridYSpace - 3, 10, 10);
        // Top Mid
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.0234375F, 0.59375F, 0.04296875F, 0.61328125F), 10, gridYSpace - 3, 255 - 10, 10);
        // Top Right
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.046875F, 0.59375F, 0.06640625F, 0.61328125F), 255, gridYSpace - 3, 10, 10);
    }

    private void AddMidBottomBackground(int topBackGround, int gridYSpace, PlayerQuestInnerPanel plainPanel) {
        // Mid Left
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.0F, 0.6171875F, 0.01953125F, 0.634765625F), 0, topBackGround, 10, gridYSpace - topBackGround);
        // Mid Mid
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.0234375F, 0.6171875F, 0.04296875F, 0.634765625F), 10, topBackGround, 255 - 10, gridYSpace - topBackGround);
        // Mid Right
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.046875F, 0.6171875F, 0.06640625F, 0.634765625F), 255, topBackGround, 10, gridYSpace - topBackGround);
        // Bottom Left
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.0F, 0.640625F, 0.01953125F, 0.66015625F), 0, gridYSpace, 10, 10);
        // Bottom Mid
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.0234375F, 0.640625F, 0.04296875F, 0.66015625F), 10, gridYSpace, 255 - 10, 10);
        // Bottom Right
        plainPanel.add(new WSprite(QuestScreenHandler.GUI_ICONS, 0.046875F, 0.640625F, 0.06640625F, 0.66015625F), 255, gridYSpace, 10, 10);
    }
}
