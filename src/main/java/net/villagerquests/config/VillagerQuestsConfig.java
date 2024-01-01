package net.villagerquests.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "villagerquests")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class VillagerQuestsConfig implements ConfigData {

    public int test = 0;
    public int test2 = 0;
    public int test3 = 0;
    public int test4 = 0;
    public int test5 = 0;

    @Comment("in ticks")
    public int villagerQuestGlowTime = 60;
    @Comment("Area to look for glowing Villager")
    public int villagerQuestBoxSize = 32;
    public boolean showQuestIcon = true;
    @Comment("Flat or 3d icon")
    public boolean flatQuestIcon = true;
    @Comment("Squared distance showing up quest icon")
    public int iconDistace = 30;

}