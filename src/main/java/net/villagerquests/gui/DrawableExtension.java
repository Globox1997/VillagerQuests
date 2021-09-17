package net.villagerquests.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

// Made by Haven King: https://gist.github.com/Haven-King/ac5a38e0f902af298feff45031f57bf2

public interface DrawableExtension {

    static void draw(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
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
        // DrawableHelper.drawCenteredText(matrices, textRenderer, text, 0, -textRenderer.fontHeight / 2, color);
        // textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
        // textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
        textRenderer.draw(matrices, text, (float) (-textRenderer.getWidth(text) / 2), (float) -textRenderer.fontHeight / 2, color);
        matrices.pop();
    }

    static void renderQuestItems(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, ItemRenderer itemRenderer, ItemStack itemStack, int x, int y, float scale) {
        matrices.push();
        matrices.scale(scale, scale, 1F);
        matrices.translate(x / scale, (y + 16 / 2F) / scale, 0);
        // itemRenderer.renderInGui(itemStack, x, y);

        // LightmapTextureManager.MAX_LIGHT_COORDINATE
        itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GUI, 100, OverlayTexture.DEFAULT_UV, matrices, vertexConsumerProvider, 0);
        // public void renderItem(ItemStack stack, ModelTransformation.Mode transformationType, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int seed) {
        // this.renderItem((LivingEntity)null, stack, transformationType, false, matrices, vertexConsumers, (World)null, light, overlay, seed);
        // }
        matrices.pop();
    }
}
