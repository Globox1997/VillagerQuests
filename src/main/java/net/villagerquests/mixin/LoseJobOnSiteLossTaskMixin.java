package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.villagerquests.accessor.MerchantAccessor;

@Mixin(LoseJobOnSiteLossTask.class)
public class LoseJobOnSiteLossTaskMixin {

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    protected void runMixin(ServerWorld serverWorld, VillagerEntity villagerEntity, long l, CallbackInfo info) {
        ((MerchantAccessor) villagerEntity).clearQuestList();
    }

}