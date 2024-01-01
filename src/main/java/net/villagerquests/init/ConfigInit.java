package net.villagerquests.init;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.villagerquests.config.VillagerQuestsConfig;

public class ConfigInit {

    public static VillagerQuestsConfig CONFIG = new VillagerQuestsConfig();

    public static void init() {
        AutoConfig.register(VillagerQuestsConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VillagerQuestsConfig.class).getConfig();
    }

}
