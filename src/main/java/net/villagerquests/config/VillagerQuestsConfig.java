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

    public int test0 = 0;
    public int test1 = 0;
    public int test2 = 0;
    public int test3 = 0;
    public int test4 = 0;
    public int test5 = 0;
    public int test6 = 0;
    public int test7 = 0;

    public int s0 = 0;
    public int s1 = 0;
    public int s2 = 0;
    public int s3 = 0;
    public int s4 = 0;
    public int s5 = 0;
    public int s6 = 0;

    public float s10 = 1.0F;
    public float s11 = 1.0F;
    public float s12 = 1.0F;

    @Comment("Villager will remember quests")
    public boolean rememberQuests = true;

}