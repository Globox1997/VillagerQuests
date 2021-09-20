package net.villagerquests.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.villagerquests.VillagerQuestsClient;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.feature.QuestEntityModel;

@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerEntityRendererMixin extends MobEntityRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>> {
    private static final Identifier QUEST_TEXTURE = new Identifier("villagerquests:textures/entity/quest.png");
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
        if (VillagerQuestsMain.CONFIG.showQuestIcon) {
            if (VillagerQuestsMain.CONFIG.flatQuestIcon) {
                matrixStack.push();
                float height = mobEntity.getHeight() + 0.5F + VillagerQuestsMain.CONFIG.s13;
                matrixStack.translate(0.0D, height, 0.0D);
                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-VillagerQuestsMain.CONFIG.s12, -VillagerQuestsMain.CONFIG.s12, VillagerQuestsMain.CONFIG.s12);
                Matrix4f matrix4f = matrixStack.peek().getModel();
                TextRenderer textRenderer = this.getTextRenderer();
                float h = (float) (-textRenderer.getWidth((StringVisitable) Text.of("?")) / 2);
                textRenderer.draw(Text.of("?"), h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, false, 0, i);
                matrixStack.pop();
            } else {
                matrixStack.push();
                float height = mobEntity.getHeight() + -0.5F;
                matrixStack.translate(0.0D, height, 0.0D);
                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-VillagerQuestsMain.CONFIG.s14, -VillagerQuestsMain.CONFIG.s14, VillagerQuestsMain.CONFIG.s14);
                VertexConsumer vertexConsumers = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(QUEST_TEXTURE));
                this.questModel.render(matrixStack, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                this.questModel.setAngles(mobEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                matrixStack.pop();
            }
        }

    }

}
