package net.villagerquests.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.feature.QuestEntityModel;
import net.villagerquests.init.ConfigInit;

public class QuestRenderHelper {

    private static final Identifier QUEST_TEXTURE = new Identifier("villagerquests:textures/entity/quest_mark.png");

    public static void renderQuestMark(MerchantEntity merchantEntity, MatrixStack matrixStack, EntityRenderDispatcher dispatcher, TextRenderer textRenderer,
            VertexConsumerProvider vertexConsumerProvider, QuestEntityModel<MerchantEntity> questEntityModel, boolean hasLabel, int i) {
        if (ConfigInit.CONFIG.showQuestIcon) {
            int questMarkType = ((MerchantAccessor) merchantEntity).getQuestMarkType();
            if (questMarkType > 0 && Math.sqrt(dispatcher.getSquaredDistanceToCamera(merchantEntity)) < ConfigInit.CONFIG.iconDistance
                    && merchantEntity.getWorld().getBlockState(merchantEntity.getBlockPos().up(2)).isAir()) {
                MinecraftClient client = MinecraftClient.getInstance();
                PlayerEntity player = client.player;

                if (player != null) {
                    matrixStack.push();
                    float height = ConfigInit.CONFIG.flatQuestIcon ? merchantEntity.getHeight() + 1.1F : merchantEntity.getHeight() + 2.0F;
                    matrixStack.translate(0.0D, height, 0.0D);

                    if (hasLabel) {
                        matrixStack.translate(0.0D, 0.3D, 0.0D);
                    }
                    matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(toEulerXyzDegrees(dispatcher.getRotation()).y()));

                    if (ConfigInit.CONFIG.flatQuestIcon) {
                        matrixStack.scale(-0.1F, -0.1F, 0.1F);
                    } else {
                        matrixStack.scale(-1.0F, -1.0F, 1.0F);
                    }
                    Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                    VertexConsumer vertexConsumers = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(QUEST_TEXTURE));

                    if (ConfigInit.CONFIG.flatQuestIcon) {
                        Text text = Text.of("?");
                        if (questMarkType == 2) {
                            text = Text.of("!");
                        }
                        float h = (float) (-textRenderer.getWidth(text) / 2);
                        textRenderer.draw(text, h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, TextLayerType.NORMAL, 0, i);
                    } else {
                        questEntityModel.questionMark = true;
                        if (questMarkType == 2) {
                            questEntityModel.questionMark = false;
                        }
                        questEntityModel.render(matrixStack, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                        questEntityModel.setAngles(merchantEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                    }
                    matrixStack.pop();
                }

            }
        }
    }

    // Could get moved to libz since usage in TalkBubbles

    private static Vector3f toEulerXyzDegrees(Quaternionf quaternionf) {
        Vector3f vec3f = toEulerXyz(quaternionf);
        return new Vector3f((float) Math.toDegrees(vec3f.x()), (float) Math.toDegrees(vec3f.y()), (float) Math.toDegrees(vec3f.z()));
    }

    private static Vector3f toEulerXyz(Quaternionf quaternionf) {
        float f = quaternionf.w() * quaternionf.w();
        float g = quaternionf.x() * quaternionf.x();
        float h = quaternionf.y() * quaternionf.y();
        float i = quaternionf.z() * quaternionf.z();
        float j = f + g + h + i;
        float k = 2.0f * quaternionf.w() * quaternionf.x() - 2.0f * quaternionf.y() * quaternionf.z();
        float l = (float) Math.asin(k / j);
        if (Math.abs(k) > 0.999f * j) {
            return new Vector3f(l, 2.0f * (float) Math.atan2(quaternionf.y(), quaternionf.w()), 0.0f);
        }
        return new Vector3f(l, (float) Math.atan2(2.0f * quaternionf.x() * quaternionf.z() + 2.0f * quaternionf.y() * quaternionf.w(), f - g - h + i),
                (float) Math.atan2(2.0f * quaternionf.x() * quaternionf.y() + 2.0f * quaternionf.w() * quaternionf.z(), f - g + h - i));
    }

}
