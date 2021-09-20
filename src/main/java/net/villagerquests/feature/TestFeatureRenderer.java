// package net.villagerquests.feature;

// import com.mojang.blaze3d.systems.RenderSystem;

// import net.fabricmc.api.EnvType;
// import net.fabricmc.api.Environment;
// import net.minecraft.block.Blocks;
// import net.minecraft.client.MinecraftClient;
// import net.minecraft.client.font.TextRenderer;
// import net.minecraft.client.gui.DrawableHelper;
// import net.minecraft.client.model.ModelPart;
// import net.minecraft.client.particle.ParticleManager;
// import net.minecraft.client.render.BufferBuilder;
// import net.minecraft.client.render.BufferRenderer;
// import net.minecraft.client.render.Tessellator;
// import net.minecraft.client.render.VertexConsumerProvider;
// import net.minecraft.client.render.VertexFormat;
// import net.minecraft.client.render.VertexFormats;
// import net.minecraft.client.render.entity.PaintingEntityRenderer;
// import net.minecraft.client.render.entity.feature.FeatureRenderer;
// import net.minecraft.client.render.entity.feature.FeatureRendererContext;
// import net.minecraft.client.render.entity.model.EntityModel;
// import net.minecraft.client.util.math.MatrixStack;
// import net.minecraft.entity.LivingEntity;
// import net.minecraft.entity.mob.PiglinEntity;
// import net.minecraft.text.StringVisitable;
// import net.minecraft.text.Text;
// import net.minecraft.util.math.Matrix4f;
// import net.minecraft.util.math.Vec3f;
// import net.villagerquests.gui.QuestScreenHandler;

// @Environment(EnvType.CLIENT)
// public class TestFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
//     private final TextRenderer textRenderer;
//     private final ModelPart modelPart;

//     public TestFeatureRenderer(FeatureRendererContext<T, M> featureRendererContext, TextRenderer textRenderer, ModelPart modelPart) {
//         super(featureRendererContext);
//         this.textRenderer = textRenderer;
//         this.modelPart = modelPart;
//     }

//     @Override
//     public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
//         // float height = livingEntity.getHeight() + 0.5F;
//         // matrices.push();
//         // matrices.translate(0.0D, -(double) height, 0.0D);

//         // matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(modelPart.yaw / (float) (Math.PI * 2F) * 360.0F));

//         // matrices.scale(0.025F, 0.025F, 0.025F);
//         // Matrix4f matrix4f = matrices.peek().getModel();

//         // // float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
//         // // int j = (int) (g * 255.0F) << 24;

//         // Text text = Text.of("TESTESTEST");
//         // float width = (float) (-textRenderer.getWidth((StringVisitable) text) / 2);
//         // // textRenderer.draw(text, width, 0.0F, 553648127, false, matrix4f, vertexConsumerProvider, true, (int) j, i);
//         // textRenderer.draw((Text) text, width, 0.0F, -1, false, matrix4f, vertexConsumerProvider, false, 0, i);

//         // // public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
//         // DrawableHelper.drawTexture();

//         // matrices.pop();
//         // System.out.println(livingEntity.world.getBlockState(livingEntity.getBlockPos().up(2)));
//         float height = livingEntity.getHeight() + 0.5F;
//         matrices.push();
//         modelPart.rotate(matrices);
//         matrices.translate(0.0D, -(double) height, 0.0D);

//         // PaintingEntityRenderer
//         // matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(modelPart.yaw / (float) (Math.PI * 2F) * 360.0F));

//         // matrices.scale(0.025F, 0.025F, 0.025F);
//         Matrix4f matrix4f = matrices.peek().getModel();

//         // float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
//         // int j = (int) (g * 255.0F) << 24;

//         // Text text = Text.of("TESTESTEST");
//         // float width = (float) (-textRenderer.getWidth((StringVisitable) text) / 2);
//         // textRenderer.draw(text, width, 0.0F, 553648127, false, matrix4f, vertexConsumerProvider, true, (int) j, i);
//         // textRenderer.draw((Text) text, width, 0.0F, -1, false, matrix4f, vertexConsumerProvider, false, 0, i);

//         // public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
//         // public static void drawTexture(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight)
//         // DrawableHelper.drawTexture(matrices,1,1,1,1,1F,1F,1,1,1,1);

//         // VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(this.getTexture(paintingEntity)));
//         // PaintingManager paintingManager = MinecraftClient.getInstance().getPaintingManager();
//         // this.renderPainting(matrixStack, vertexConsumer, paintingEntity, paintingMotive.getWidth(), paintingMotive.getHeight(), paintingManager.getPaintingSprite(paintingMotive),
//         // paintingManager.getBackSprite());
//         // matrixStack.pop();

//         // RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
//         // DrawableHelper.drawTexture(matrices, 0, (int) height, 0, 0, 20, 20, 20, 20, 256, 256);

//         // RenderSystem.setShader(GameRenderer::getPositionTexShader);

//         // BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
//         // bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
//         // bufferBuilder.vertex(matrix4f, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
//         // bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
//         // bufferBuilder.vertex(matrix4f, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
//         // bufferBuilder.vertex(matrix4f, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
//         // bufferBuilder.end();
//         // BufferRenderer.draw(bufferBuilder);
//         // PiglinEntity

//         matrices.pop();

//     }
// }

package net.villagerquests.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class TestFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public TestFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw,
            float headPitch) {

    }

    // public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, HorseEntity horseEntity, float f, float g, float h, float j, float k, float l) {
    // Identifier identifier = (Identifier)TEXTURES.get(horseEntity.getMarking());
    // if (identifier != null && !horseEntity.isInvisible()) {
    // VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(identifier));
    // ((HorseEntityModel)this.getContextModel()).render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(horseEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    // }
    // }
}
