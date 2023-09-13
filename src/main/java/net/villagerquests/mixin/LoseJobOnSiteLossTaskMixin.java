package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.villagerquests.accessor.MerchantAccessor;

@Mixin(LoseJobOnSiteLossTask.class)
public class LoseJobOnSiteLossTaskMixin {

    // lambda injection
    @Inject(method = "method_47038", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setVillagerData(Lnet/minecraft/village/VillagerData;)V"))
    private static void runMixin(ServerWorld serverWorld, VillagerEntity villagerEntity, long time, CallbackInfoReturnable<Boolean> info) {
        ((MerchantAccessor) villagerEntity).clearQuestList();
    }

}