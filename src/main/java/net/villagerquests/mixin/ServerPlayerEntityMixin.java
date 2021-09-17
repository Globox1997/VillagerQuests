package net.villagerquests.mixin;

import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.network.QuestServerPacket;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    // @Inject(method = "openHandledScreen", at = @At("HEAD"))
    // private void openHandledScreenMixin(@Nullable NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> info) {
    // System.out.println("JAND");
    // if (factory != null && ((PlayerEntity) (Object) this).currentScreenHandler.getType().equals(ScreenHandlerType.MERCHANT)) {
    // System.out.println("OKEEE");

    // }
    // // return OptionalInt.empty();
    // }

    @Inject(method = "onSpawn", at = @At(value = "TAIL"))
    private void onSpawnMixin(CallbackInfo info) {
        System.out.println("ON SPAWN");
        QuestServerPacket.writeS2CPlayerQuestDataPacket((ServerPlayerEntity) (Object) this);
    }

}
