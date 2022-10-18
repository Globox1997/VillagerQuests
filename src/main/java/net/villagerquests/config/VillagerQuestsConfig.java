package net.villagerquests.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "villagerquests")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class VillagerQuestsConfig implements ConfigData {

    @Comment("Villager will remember quests")
    public boolean rememberQuests = true;
    @Comment("Villager can only level up when at least one quest was finished")
    public boolean canOnlyLevelUpWhenCompleted = false;
    public boolean canOnlyAddLevelSpecificQuests = false;
    @Comment("Chance a merchant doesn't offer quests : 0.2 = 20%")
    public float noQuestChance = 0.2F;
    @Comment("Time in ticks (+ random) when a villager can offer new quests")
    public int newQuestTimer = 36000;
    @Comment("Extra quantity of quests which get added on level up")
    public int villagerQuestExtraQuantity = 1;
    @Comment("Wandering trader quest quantity")
    public int wanderingQuestQuantity = 6;
    @Comment("Time in ticks added to wandering trader despawn ticker")
    public int wanderingTraderDespawnAddition = 48000;
    public boolean showQuestIcon = true;
    @Comment("Flat or 3d icon")
    public boolean flatQuestIcon = true;
    @Comment("Squared distance showing up quest icon")
    public double iconDistace = 300.0D;
    @Comment("Loose quests on death")
    public boolean hardMode = false;

    @ConfigEntry.Category("gui_setting")
    @Comment("Set to -20 if ontop of screen")
    public int xIconPosition = 0;
    @ConfigEntry.Category("gui_setting")
    @Comment("Set to -20 if ontop of screen")
    public int yIconPosition = 0;
    @ConfigEntry.Category("gui_setting")
    @ConfigEntry.Gui.RequiresRestart
    @Comment("REI/EMI exclusion zone")
    public boolean exclusionZone = true;
    @ConfigEntry.Category("gui_setting")
    public boolean showQuestItems = true;
    @ConfigEntry.Category("gui_setting")
    public int questTabTitleColor = 16777215;
    @ConfigEntry.Category("gui_setting")
    public int titleColor = 16777215;
    @ConfigEntry.Category("gui_setting")
    public int descriptionColor = 14540253;
    @ConfigEntry.Category("gui_setting")
    public int taskHeaderColor = 4210752;
    @ConfigEntry.Category("gui_setting")
    public int taskColor = 14540253;
    @ConfigEntry.Category("gui_setting")
    public int rewardHeaderColor = 4210752;
    @ConfigEntry.Category("gui_setting")
    public int rewardColor = 14540253;

    @ConfigEntry.Category("gui_setting")
    public int playerInfoColor = 16777215;
    @ConfigEntry.Category("gui_setting")
    public int playerActiveColor = 4161378;
    @ConfigEntry.Category("gui_setting")
    public int playerFinishedHeaderColor = 8352831;
    @ConfigEntry.Category("gui_setting")
    public int playerFinishedColor = 14211288;
    @ConfigEntry.Category("gui_setting")
    public int playerTitleColor = 2631720;
    @ConfigEntry.Category("gui_setting")
    public int playerDescriptionHeaderColor = 4013373;
    @ConfigEntry.Category("gui_setting")
    public int playerDescriptionColor = 14211288;
    @ConfigEntry.Category("gui_setting")
    public int playerTaskHeaderColor = 4013373;
    @ConfigEntry.Category("gui_setting")
    public int playerTaskColor = 14211288;
    @ConfigEntry.Category("gui_setting")
    public int playerRewardHeaderColor = 4013373;
    @ConfigEntry.Category("gui_setting")
    public int playerRewardColor = 14211288;
    @ConfigEntry.Category("gui_setting")
    public int playerRefreshHeaderColor = 8345919;
    @ConfigEntry.Category("gui_setting")
    public int playerRefreshColor = 14211288;
    @ConfigEntry.Category("gui_setting")
    @Comment("Prints in console - only builtin")
    public boolean structureRegistryCheck = false;
}