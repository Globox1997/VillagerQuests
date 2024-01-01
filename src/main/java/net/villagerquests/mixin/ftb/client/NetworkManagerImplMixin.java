package net.villagerquests.mixin.ftb.client;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.architectury.networking.fabric.NetworkManagerImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(NetworkManagerImpl.class)
public class NetworkManagerImplMixin {

    @Redirect(method = "registerS2CReceiver", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"), remap = false)
    private static void loggerS2CMixin(Logger logger, String text, Object obj) {
    }

}
