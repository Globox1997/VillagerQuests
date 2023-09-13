// package net.villagerquests.gui;

// import com.mojang.blaze3d.systems.RenderSystem;

// import net.fabricmc.api.Environment;
// import net.fabricmc.api.EnvType;
// import net.minecraft.client.util.math.MatrixStack;
// import net.villagerquests.VillagerQuestsClient;

// @Environment(EnvType.CLIENT)
// public class PlayerQuestScreen extends CottonClientScreen {
//     public PlayerQuestScreen(GuiDescription description) {
//         super(description);
//     }

//     @Override
//     public boolean shouldPause() {
//         return false;
//     }

//     @Override
//     public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
//         super.render(matrices, mouseX, mouseY, partialTicks);

//         int scaledWidth = this.client.getWindow().getScaledWidth();
//         int scaledHeight = this.client.getWindow().getScaledHeight();

//         // RenderSystem.setShaderTexture(0, new Identifier("textures/gui/book.png"));
//         // PlayerQuestScreen.drawTexture(matrices, scaledWidth / 2 - 153, scaledHeight / 2 - 109, 20, 1, 146, 180, 256, 256);

//         RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
//         PlayerQuestScreen.drawTexture(matrices, scaledWidth / 2 - 153, scaledHeight / 2 - 109, 0, 256, 306, 6, 512, 512);
//         PlayerQuestScreen.drawTexture(matrices, scaledWidth / 2 - 153, scaledHeight / 2 + 104, 0, 262, 306, 6, 512, 512);
//     }

//     @Override
//     public boolean keyPressed(int ch, int keyCode, int modifiers) {
//         if (VillagerQuestsClient.questKey.matchesKey(ch, keyCode) || client.options.inventoryKey.matchesKey(ch, keyCode)) {
//             this.close();
//             return true;
//         } else
//             return super.keyPressed(ch, keyCode, modifiers);

//     }

// }