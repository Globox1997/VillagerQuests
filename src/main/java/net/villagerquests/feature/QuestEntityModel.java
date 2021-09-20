package net.villagerquests.feature;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;

@Environment(EnvType.CLIENT)
public class QuestEntityModel<T extends MerchantEntity> extends EntityModel<T> {
    private final ModelPart exclamation;
    private final ModelPart question;

    public QuestEntityModel(ModelPart root) {
        this.exclamation = root.getChild("exclamation");
        this.question = root.getChild("question");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("exclamation", ModelPartBuilder.create().uv(0, 4).cuboid(-1.0F, -13.0F, -1.0F, 2.0F, 9.0F, 2.0F).uv(14, 12).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        modelPartData.addChild("question",
                ModelPartBuilder.create().uv(14, 8).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F).uv(6, 14).cuboid(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F).uv(0, 0)
                        .cuboid(-3.0F, -14.0F, -1.0F, 6.0F, 2.0F, 2.0F).uv(8, 10).cuboid(-5.0F, -12.0F, -1.0F, 2.0F, 2.0F, 2.0F).uv(14, 2).cuboid(1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F).uv(8, 4)
                        .cuboid(3.0F, -12.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.exclamation.render(matrices, vertices, light, overlay);
        this.question.render(matrices, vertices, light, overlay);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.question.visible = false;
        this.exclamation.visible = false;
        this.exclamation.pivotY = VillagerQuestsMain.CONFIG.s10;
        this.question.pivotY = VillagerQuestsMain.CONFIG.s10;
        PlayerEntity player = MinecraftClient.getInstance().player;
        List<Integer> merchantQuestList = ((MerchantAccessor) entity).getQuestIdList();

        // System.out.print(merchantQuestList);
        // System.out.println(entity.world.getEntityById(5));
        //
        if (player != null && !merchantQuestList.isEmpty()) {
            List<Integer> finishedQuestIdList = ((PlayerAccessor) player).getPlayerFinishedQuestIdList();
            List<Integer> questIdList = ((PlayerAccessor) player).getPlayerQuestIdList();
            for (int i = 0; i < merchantQuestList.size(); i++) {
                int questId = merchantQuestList.get(i);
                boolean containsQuest = questIdList.contains(questId);
                if (!containsQuest && !finishedQuestIdList.contains(questId)) {
                    this.question.visible = true;
                    break;
                } else if (containsQuest && ((PlayerAccessor) player).isOriginalQuestGiver(entity.getUuid(), questId) && Quest.getQuestById(questId).canCompleteQuest(player)) {
                    this.exclamation.visible = true;
                }
            }
            if ((this.question.visible || this.exclamation.visible) && entity.hasCustomName()) {
                this.exclamation.pivotY = VillagerQuestsMain.CONFIG.s11;
                this.question.pivotY = VillagerQuestsMain.CONFIG.s11;
            }
        }
    }
}
