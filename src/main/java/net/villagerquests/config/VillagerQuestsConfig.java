package net.villagerquests.config;

import me.shedaniel.autoconfig.ConfigData;
//import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "villagerquests")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class VillagerQuestsConfig implements ConfigData {
    // @ConfigEntry.Category("level_setting")

    public int test0 = 14;
    public int test1 = 14;
    public int test2 = 14;
    public int test3 = 10;
    public int test4 = 0;
    public int test5 = 0;
    public int test6 = 0;
    public int test7 = 0;

    @Comment("Villager will remember quests")
    public boolean rememberQuests = true;
    @Comment("Time in ticks added to wandering trader despawn ticker")
    public int wanderingTraderDespawnAddition = 48000;
    public boolean showQuestIcon = true;
    @Comment("Flat or 3d icon")
    public boolean flatQuestIcon = true;
    @Comment("Squared distance showing up quest icon")
    public double iconDistace = 300.0D;

}