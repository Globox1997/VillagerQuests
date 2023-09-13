package net.villagerquests.util;

import java.util.List;

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
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.feature.QuestEntityModel;

public class QuestRenderHelper {

    private static final Identifier QUEST_TEXTURE = new Identifier("villagerquests:textures/entity/quest.png");

    public static void renderQuestMark(MerchantEntity mobEntity, MatrixStack matrixStack, EntityRenderDispatcher dispatcher, TextRenderer textRenderer, VertexConsumerProvider vertexConsumerProvider,
            QuestEntityModel<MerchantEntity> questEntityModel, boolean hasLabel, int i) {
        if (VillagerQuestsMain.CONFIG.showQuestIcon && dispatcher.getSquaredDistanceToCamera(mobEntity) < VillagerQuestsMain.CONFIG.iconDistace
                && mobEntity.getWorld().getBlockState(mobEntity.getBlockPos().up(2)).isAir()) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            PlayerEntity player = minecraftClient.player;
            List<Integer> merchantQuestList = ((MerchantAccessor) mobEntity).getQuestIdList();
            if (player != null && !merchantQuestList.isEmpty()) {
                List<Integer> playerFinishedQuestIdList = ((PlayerAccessor) player).getPlayerFinishedQuestIdList();
                List<Integer> playerQuestIdList = ((PlayerAccessor) player).getPlayerQuestIdList();
                matrixStack.push();
                float height = VillagerQuestsMain.CONFIG.flatQuestIcon ? mobEntity.getHeight() + 1.1F : mobEntity.getHeight() + 2.0F;
                matrixStack.translate(0.0D, height, 0.0D);

                if (hasLabel) {
                    matrixStack.translate(0.0D, 0.3D, 0.0D);
                }
                // method 35828 is the last Vec3f method in the Quaternion class
                // matrixStack.multiply(RotationAxis.POSITIVE_Y.getDegreesQuaternion(dispatcher.getRotation().toEulerXyzDegrees().getY()));
                // matrixStack.multiply(RotationAxis.POSITIVE_Y.getDegreesQuaternion(dispatcher.getRotation().toEulerXyzDegrees().getY()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(toEulerXyzDegrees(dispatcher.getRotation()).y()));
                // matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(toEulerXyzDegrees(entityRenderDispatcher.getRotation()).y()));

                if (VillagerQuestsMain.CONFIG.flatQuestIcon) {
                    matrixStack.scale(-0.1F, -0.1F, 0.1F);
                } else {
                    matrixStack.scale(-1.0F, -1.0F, 1.0F);
                }
                Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                VertexConsumer vertexConsumers = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(QUEST_TEXTURE));

                for (int u = 0; u < merchantQuestList.size(); u++) {
                    int questId = merchantQuestList.get(u);
                    boolean containsQuest = playerQuestIdList.contains(questId);
                    if (containsQuest && ((PlayerAccessor) player).isOriginalQuestGiver(mobEntity.getUuid(), questId) && Quest.getQuestById(questId).canCompleteQuest(player)) {
                        if (VillagerQuestsMain.CONFIG.flatQuestIcon) {
                            float h = (float) (-textRenderer.getWidth(Text.of("!")) / 2);
                            // textRenderer.draw(Text.of("!", x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light)
                            textRenderer.draw(Text.of("!"), h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, TextLayerType.NORMAL, 0, i);
                        } else {
                            questEntityModel.questionMark = false;
                            questEntityModel.render(matrixStack, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                            questEntityModel.setAngles(mobEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                        }
                    } else if (!containsQuest && !playerFinishedQuestIdList.contains(questId)) {
                        if (VillagerQuestsMain.CONFIG.flatQuestIcon) {
                            float h = (float) (-textRenderer.getWidth(Text.of("?")) / 2);
                            // textRenderer.draw(Text.of("?"), h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, false, 0, i);
                            textRenderer.draw(Text.of("?"), h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, TextLayerType.NORMAL, 0, i);
                        } else {
                            questEntityModel.questionMark = true;
                            questEntityModel.render(matrixStack, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                            questEntityModel.setAngles(mobEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                        }
                        break;
                    }
                }
                matrixStack.pop();
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
