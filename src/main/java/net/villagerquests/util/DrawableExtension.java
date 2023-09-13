package net.villagerquests.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.villagerquests.VillagerQuestsMain;

public interface DrawableExtension {

    static void drawText(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1F);
        context.getMatrices().translate(centerX / scale, (y + textRenderer.fontHeight / 2F) / scale, 0);
        context.drawText(textRenderer, text, 0, -textRenderer.fontHeight / 2, color, false);
        context.getMatrices().pop();
    }

    static void drawCenteredText(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1F);
        context.getMatrices().translate(centerX / scale, (y + textRenderer.fontHeight / 2F) / scale, 0);
        context.drawText(textRenderer, text, (-textRenderer.getWidth(text) / 2), -textRenderer.fontHeight / 2, color, false);
        context.getMatrices().pop();
    }

    static void drawCenteredTextWithShadow(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color, float scale) {
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1F);
        context.getMatrices().translate(centerX / scale, (y + textRenderer.fontHeight / 2F) / scale, 0);
        context.drawText(textRenderer, text, (-textRenderer.getWidth(text) / 2), -textRenderer.fontHeight / 2, color, true);
        context.getMatrices().pop();
    }

    static void renderQuestItems(DrawContext context, ItemStack itemStack, double x, double y, float scale) {
        if (VillagerQuestsMain.CONFIG.showQuestItems) {
            context.getMatrices().push();
            x = x / 16.0D;
            y = y / 16.0D;
            context.getMatrices().scale(scale, scale, 1.0F);
            context.getMatrices().translate(x, y, 0.0D);
            context.drawItem(itemStack, 0, 0);
            context.getMatrices().pop();
        }
    }
}
