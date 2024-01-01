package net.villagerquests.mixin.ftb.client;

import org.spongepowered.asm.mixin.Mixin;

import dev.ftb.mods.ftblibrary.config.BooleanConfig;
import dev.ftb.mods.ftblibrary.config.ConfigWithVariants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(BooleanConfig.class)
public abstract class BooleanConfigMixin extends ConfigWithVariants<Boolean> {

    @Override
    public String getTooltip() {
        String location = this.getNameKey() + ".tooltip.location";
        if (I18n.hasTranslation(location)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                return Text.translatable(location, (int) client.player.getX(), (int) client.player.getY(), (int) client.player.getZ()).getString();
            }
        }
        return super.getTooltip();
    }

}
