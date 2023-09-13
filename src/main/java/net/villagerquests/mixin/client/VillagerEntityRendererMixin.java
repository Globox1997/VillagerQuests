package net.villagerquests.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.villagerquests.VillagerQuestsClient;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.util.QuestRenderHelper;

@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerEntityRendererMixin extends MobEntityRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>> {

    private QuestEntityModel<MerchantEntity> questModel;

    public VillagerEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.5F);

    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstructor(EntityRendererFactory.Context context, CallbackInfo info) {
        this.questModel = new QuestEntityModel<>(context.getModelLoader().getModelPart(VillagerQuestsClient.QUEST_LAYER));
    }

    @Override
    public void render(VillagerEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
        QuestRenderHelper.renderQuestMark(mobEntity, matrixStack, dispatcher, this.getTextRenderer(), vertexConsumerProvider, this.questModel, this.hasLabel(mobEntity), i);
    }

}
