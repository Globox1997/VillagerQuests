package net.villagerquests.mixin.ftb;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;

@SuppressWarnings("unused")
@Mixin(ConfigGroup.class)
public class ConfigGroupMixin {

    // @Inject(method = "save", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;save(Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    // private void saveMixin(boolean accepted, CallbackInfo info, Iterator var2, ConfigGroup group) {
    // System.out.println("ON SAVE: " + group);
    // if (group.getId().equals("villager_quest")) {
    // System.out.println("ON SAVE: ??? " + group);
    // }
    // }
}
