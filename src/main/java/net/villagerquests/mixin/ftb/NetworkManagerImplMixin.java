package net.villagerquests.mixin.ftb;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(targets = "dev/architectury/networking/fabric/NetworkManagerImpl$1", remap = false)
public class NetworkManagerImplMixin {

    @Redirect(method = "registerC2S", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"), remap = false)
    private static void loggerC2SMixin(Logger logger, String text, Object obj) {
    }

}
