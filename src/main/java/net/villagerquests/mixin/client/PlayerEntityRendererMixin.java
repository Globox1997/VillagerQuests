package net.villagerquests.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.entity.passive.WanderingTraderEntity;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    // protected void renderLabelIfPresent(AbstractClientPlayerEntity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
    // double d = this.dispatcher.getSquaredDistanceToCamera(abstractClientPlayerEntity);
    // matrixStack.push();
    // if (d < 100.0D) {
    // Scoreboard scoreboard = abstractClientPlayerEntity.getScoreboard();
    // ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(2);
    // if (scoreboardObjective != null) {
    // ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(abstractClientPlayerEntity.getEntityName(), scoreboardObjective);
    // super.renderLabelIfPresent(abstractClientPlayerEntity, (new LiteralText(Integer.toString(scoreboardPlayerScore.getScore()))).append(" ").append(scoreboardObjective.getDisplayName()),
    // matrixStack, vertexConsumerProvider, i);
    // Objects.requireNonNull(this.getTextRenderer());
    // matrixStack.translate(0.0D, (double)(9.0F * 1.15F * 0.025F), 0.0D);
    // }
    // }

    // super.renderLabelIfPresent(abstractClientPlayerEntity, text, matrixStack, vertexConsumerProvider, i);
    // matrixStack.pop();
    // }
}
