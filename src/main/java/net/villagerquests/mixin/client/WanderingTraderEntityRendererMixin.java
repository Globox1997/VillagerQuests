package net.villagerquests.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.villagerquests.VillagerQuestsClient;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.util.QuestRenderHelper;

@Mixin(WanderingTraderEntityRenderer.class)
public abstract class WanderingTraderEntityRendererMixin extends MobEntityRenderer<WanderingTraderEntity, VillagerResemblingModel<WanderingTraderEntity>> {
    private QuestEntityModel<MerchantEntity> questModel;

    public WanderingTraderEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.WANDERING_TRADER)), 0.5F);

    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstructor(EntityRendererFactory.Context context, CallbackInfo info) {
        this.questModel = new QuestEntityModel<>(context.getModelLoader().getModelPart(VillagerQuestsClient.QUEST_LAYER));
    }

    @Override
    public void render(WanderingTraderEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
        QuestRenderHelper.renderQuestMark(mobEntity, matrixStack, dispatcher, this.getTextRenderer(), vertexConsumerProvider, this.questModel, this.hasLabel(mobEntity), i);

    }

}
