package net.villagerquests.config;

import me.shedaniel.autoconfig.ConfigData;
//import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "villagerquests")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class VillagerQuestsConfig implements ConfigData {
    // @ConfigEntry.Category("level_setting")

    // @Comment("Applies if bonus chest world setting is enabled")
    // public String jong = "";
    // public boolean test = false;

    // public int test0 = 0;
    // // public int test1 = 0;
    // public int test2 = 0;
    // public int test3 = 0;
    // public int test4 = 0;
    // public int test5 = 0;
    // public int test6 = 0;
    // public int test7 = 0;

    // public int s0 = 0;
    // public int s1 = 0;
    // public int s2 = 0;
    // public double s3 = 0.0D;
    // public double s4 = 0.0D;
    // public int s5 = 0;
    // public int s6 = 0;

    // public double s7 = 0.0D;
    // public double s8 = 0.0D;
    // public double s9 = 16.0D;

    // public float s12 = 1.0F;
    // public float s13 = 1.0F;
    // public float s14 = 1.0F;
    // public float s15 = 1.0F;
    // public float s16 = 1.0F;

    public boolean test = false;

    @Comment("Villager will remember quests")
    public boolean rememberQuests = true;
    @Comment("Time in ticks added to wandering trader despawn ticker")
    public int wanderingTraderDespawnAddition = 48000;
    public boolean showQuestIcon = true;
    @Comment("Flat or 3d icon")
    public boolean flatQuestIcon = true;

}