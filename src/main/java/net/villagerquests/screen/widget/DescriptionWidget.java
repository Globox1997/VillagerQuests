package net.villagerquests.screen.widget;

import java.util.List;

import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DescriptionWidget extends ScrollableWidget {

    private List<Text> textList;
    private final TextRenderer textRenderer;

    private int totalYSpace = 0;
    private int ySpace = 0;

    public DescriptionWidget(int x, int y, int width, int height, List<Text> textList, TextRenderer textRenderer) {
        super(x, y, width, height, Text.of(""));
        this.textRenderer = textRenderer;
        this.textList = textList;
    }

    @Override
    protected void appendDefaultNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder var1) {
    }

    @Override
    protected int getContentsHeight() {
        return this.totalYSpace;
    }

    @Override
    protected boolean overflows() {
        return false;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 14;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.textList.isEmpty()) {
            this.ySpace = this.getY();
            for (int i = 0; i < textList.size(); i++) {
                boolean isImageComponent = false;
                Text text = textList.get(i);
                for (int u = 0; u < text.getSiblings().size(); u++) {
                    for (int o = 0; o < text.getSiblings().get(u).getSiblings().size(); o++) {
                        if (text.getSiblings().get(u).getSiblings().get(o).getContent() instanceof ImageComponent imageComponent) {

                            int imageX = this.getX();
                            int imageY = this.ySpace;
                            int width = imageComponent.width;
                            int height = imageComponent.height;

                            if (imageComponent.fit) {
                                float scale = (float) this.width / imageComponent.width;
                                width *= scale;
                                height *= scale;
                            } else if (imageComponent.align == 1) {
                                imageX += this.width / 2 - width / 2;
                            } else if (imageComponent.align == 2) {
                                imageX += this.width - width;
                            }
                            imageComponent.image.draw(context, imageX, imageY, width, height);
                            this.ySpace += height + 2;

                            isImageComponent = true;
                            break;
                        }
                    }
                    if (isImageComponent) {
                        break;
                    }
                }
                if (!isImageComponent) {
                    List<StringVisitable> list = Theme.DEFAULT.listFormattedStringToWidth(text, 154);
                    if (list.size() == 0) {
                        this.ySpace += 9;
                    } else {
                        for (int k = 0; k < list.size(); k++) {
                            context.drawTextWrapped(textRenderer, list.get(k), this.getX(), this.ySpace, 154, 0xFFFFFF);
                            this.ySpace += 9;
                        }
                    }
                }
            }
            this.totalYSpace = this.ySpace - this.getY();
        }
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
        context.getMatrices().push();
        context.getMatrices().translate(0.0, -getScrollY(), 0.0);
        this.renderContents(context, mouseX, mouseY, delta);
        context.getMatrices().pop();
        context.disableScissor();
        this.renderOverlay(context);
    }

    @Override
    protected void renderOverlay(DrawContext context) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.visible) {
            return false;
        }
        this.setScrollY(this.getScrollY() - amount * this.getDeltaYPerScroll());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!this.visible || !this.isFocused()) {
            return false;
        }
        if (mouseY < (double) this.getY()) {
            this.setScrollY(0.0);
        } else if (mouseY > (double) (this.getY() + this.height)) {
            this.setScrollY(this.getMaxScrollY());
        } else {
            int i = this.getScrollbarThumbHeight();
            double d = Math.max(1, this.getMaxScrollY() / (this.height - i));
            this.setScrollY(this.getScrollY() + deltaY * d);
        }
        return true;
    }

    @Override
    protected boolean isWithinBounds(double mouseX, double mouseY) {
        return mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width + 1) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
    }

    @Override
    public void setScrollY(double scrollY) {
        super.setScrollY(scrollY);
    }

    @Override
    public int getMaxScrollY() {
        return super.getMaxScrollY();
    }

    private int getContentsHeightWithPadding() {
        return this.getContentsHeight() + 4;
    }

    private int getScrollbarThumbHeight() {
        return MathHelper.clamp((int) ((float) (this.height * this.height) / (float) this.getContentsHeightWithPadding()), 32, this.height);
    }

}
