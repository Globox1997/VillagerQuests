package net.villagerquests.mixin.ftb.client;

import org.spongepowered.asm.mixin.Mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(ConfigGroup.class)
public class ConfigGroupMixin {

}
