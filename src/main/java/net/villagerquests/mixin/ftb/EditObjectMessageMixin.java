package net.villagerquests.mixin.ftb;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.architectury.networking.NetworkManager.PacketContext;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;

@Mixin(EditObjectMessage.class)
public class EditObjectMessageMixin {

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/QuestObjectBase;readData(Lnet/minecraft/nbt/NbtCompound;)V"))
    private void handleMixin(PacketContext context, CallbackInfo info) {
        // System.out.println("EDITED");
    }
}
