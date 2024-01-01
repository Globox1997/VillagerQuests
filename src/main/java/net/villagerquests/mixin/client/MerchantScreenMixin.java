package net.villagerquests.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;

@Environment(EnvType.CLIENT)
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin implements Tab {

    @Override
    public Class<?> getParentScreenClass() {
        return this.getClass();
    }

}
