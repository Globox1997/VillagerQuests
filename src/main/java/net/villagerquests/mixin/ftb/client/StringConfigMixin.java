package net.villagerquests.mixin.ftb.client;

import org.spongepowered.asm.mixin.Mixin;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigFromString;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Environment(EnvType.CLIENT)
@Mixin(StringConfig.class)
public abstract class StringConfigMixin extends ConfigFromString<String> {

    @Override
    public String getTooltip() {
        String uuid = this.getNameKey() + ".tooltip.uuid";
        if (I18n.hasTranslation(uuid)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                return Text.translatable(uuid, ((EntityHitResult) client.crosshairTarget).getEntity().getUuid()).getString();
            }
        }
        return super.getTooltip();
    }

    @Override
    public void onClicked(MouseButton button, ConfigCallback callback) {
        if (button.isRight() && this.value.equals("")) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                this.value = ((EntityHitResult) client.crosshairTarget).getEntity().getUuid().toString();
            }
        }
        super.onClicked(button, callback);
    }

}
