package net.villagerquests.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.util.math.MathHelper;
import net.villagerquests.access.MerchantAccessor;

@Environment(EnvType.CLIENT)
@Mixin(VillagerResemblingModel.class)
public abstract class VillagerResemblingModelMixin<T extends Entity> extends SinglePartEntityModel<T> {

    private boolean shouldTalk = false;

    @Shadow
    @Mutable
    @Final
    private ModelPart head;

    @Inject(method = "setAngles", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/MerchantEntity;getHeadRollingTimeLeft()I"))
    private void setAnglesMerchantMixin(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo info) {
        if (((MerchantAccessor) entity).getTalkTime() > 0) {
            this.shouldTalk = true;
        } else {
            this.shouldTalk = false;
        }
    }

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void setAnglesMixin(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo info) {
        if (this.shouldTalk && ((MerchantEntity) entity).getHeadRollingTimeLeft() <= 0) {
            this.head.pitch = Math.abs(0.45f * MathHelper.sin(0.35f * animationProgress));
        }
    }

}
