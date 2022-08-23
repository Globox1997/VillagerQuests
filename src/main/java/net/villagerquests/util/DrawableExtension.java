package net.villagerquests.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

// Made by Haven King: https://gist.github.com/Haven-King/ac5a38e0f902af298feff45031f57bf2

public interface DrawableExtension {

    static void drawText(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
        matrices.push();
        matrices.scale(scale, scale, 1F);
        matrices.translate(centerX / scale, (y + textRenderer.fontHeight / 2F) / scale, 0);
        textRenderer.draw(matrices, text, 0, -textRenderer.fontHeight / 2F, color);
        matrices.pop();
    }

    static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
        matrices.push();
        matrices.scale(scale, scale, 1F);
        matrices.translate(centerX / scale, (y + textRenderer.fontHeight / 2F) / scale, 0);
        textRenderer.draw(matrices, text, (float) (-textRenderer.getWidth(text) / 2), (float) -textRenderer.fontHeight / 2, color);
        matrices.pop();
    }

    static void drawCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
        matrices.push();
        matrices.scale(scale, scale, 1F);
        matrices.translate(centerX / scale, (y + textRenderer.fontHeight / 2F) / scale, 0);
        textRenderer.drawWithShadow(matrices, text, (float) (-textRenderer.getWidth(text) / 2), (float) -textRenderer.fontHeight / 2, color);
        matrices.pop();
    }

    static void renderQuestItems(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, ItemRenderer itemRenderer, ItemStack itemStack, double x, double y, float scale) {
        matrices.push();
        x = x / 16.0D;
        y = y / 16.0D;
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(x, y, 0.0D);
        itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GUI, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, matrices, vertexConsumerProvider, 0);
        matrices.pop();
    }
}
