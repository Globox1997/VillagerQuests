package net.villagerquests.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.lang3.text.WordUtils;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestClientPacket;

@Environment(EnvType.CLIENT)
public class QuestScreen extends CottonInventoryScreen<QuestScreenHandler> {
    private final QuestScreenHandler questScreenHandler;
    private int indexStartOffset;
    private boolean scrolling;
    private int selectedIndex;
    private final QuestScreen.WidgetButtonPage[] quests = new QuestScreen.WidgetButtonPage[7];
    private Quest selectedQuest;
    private QuestScreen.AcceptButton acceptButton;
    private List<Integer> acceptedQuestIdList;
    private boolean closedToTradeScreen = false;
    private final PlayerEntity playerEntity;

    public QuestScreen(QuestScreenHandler gui, PlayerEntity player, Text title) {
        super(gui, player, title);
        this.playerEntity = player;
        questScreenHandler = gui;
        acceptedQuestIdList = ((PlayerAccessor) player).getPlayerQuestIdList();
    }

    @Override
    public void init() {
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int k = j + 16 + 2;

        this.acceptButton = (QuestScreen.AcceptButton) this.addDrawableChild(new QuestScreen.AcceptButton(i + 142, j + 138, (button) -> {
            if (button instanceof QuestScreen.AcceptButton) {
                if (this.acceptedQuestIdList.contains(this.selectedQuest.getQuestId()) && this.selectedQuest.canCompleteQuest(this.playerEntity))
                    this.completeQuest(this.selectedIndex);
                else
                    this.acceptQuest();
            }
        }));
        this.acceptButton.setMessage(new TranslatableText("text.villagerquests.accept_button"));

        for (int l = 0; l < 7; ++l) {
            this.quests[l] = (QuestScreen.WidgetButtonPage) this.addDrawableChild(new QuestScreen.WidgetButtonPage(i + 5, k, l, (button) -> {
                if (button instanceof QuestScreen.WidgetButtonPage) {
                    this.selectedIndex = ((QuestScreen.WidgetButtonPage) button).getIndex() + this.indexStartOffset;
                    this.selectedQuest = new Quest(questScreenHandler.questIdList.get(this.selectedIndex));

                    if (this.acceptedQuestIdList.contains(this.selectedQuest.getQuestId())) {
                        if (((PlayerAccessor) this.playerEntity).isOriginalQuestGiver(this.questScreenHandler.offerer.getUuid(), this.selectedQuest.getQuestId())) {
                            this.acceptButton.setMessage(new TranslatableText("text.villagerquests.complete_button"));
                            if (this.selectedQuest.canCompleteQuest(this.playerEntity))
                                this.acceptButton.active = true;
                            else
                                this.acceptButton.active = false;
                        } else {
                            this.acceptButton.active = false;
                            this.acceptButton.unuseable = true;
                        }
                    } else {
                        this.acceptButton.active = true;
                        this.acceptButton.unuseable = false;
                        this.acceptButton.setMessage(new TranslatableText("text.villagerquests.accept_button"));
                    }
                }
            }));
            k += 20;
        }

    }

    private void acceptQuest() {
        acceptButton.setMessage(new TranslatableText("text.villagerquests.complete_button"));
        QuestClientPacket.acceptMerchantQuestC2SPacket(client.player, this.selectedQuest.getQuestId(), questScreenHandler.offerer.getUuid(), questScreenHandler.offerer.getId());
        if (this.selectedQuest.canCompleteQuest(this.playerEntity))
            this.acceptButton.active = true;
        else
            this.acceptButton.active = false;
    }

    private void completeQuest(int selectedIndex) {
        ((PlayerAccessor) this.playerEntity).finishPlayerQuest(this.selectedQuest.getQuestId());
        QuestClientPacket.writeC2SQuestCompletionPacket(this.selectedQuest.getQuestId());

        if (((PlayerAccessor) this.playerEntity).getPlayerQuestRefreshTimerList()
                .get(((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().indexOf(this.selectedQuest.getQuestId())) != -1)
            this.quests[selectedIndex].active = false;
        else
            this.questScreenHandler.questIdList.remove(selectedIndex);
        this.acceptButton.visible = false;
        this.selectedQuest = null;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        super.drawBackground(matrices, delta, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
        int i = (this.width - this.backgroundWidth) / 2 + 4;
        int j = (this.height - this.backgroundHeight) / 2 + 17;
        drawTexture(matrices, i, j, this.getZOffset(), 0.0F, 32.0F, 97, 142, 256, 256);
        drawTexture(matrices, i + 103, j, this.getZOffset(), 97.0F, 32.0F, 159, 142, 256, 256);

        // Draw trade screen button
        i = (this.width - this.backgroundWidth) / 2;
        j = (this.height - this.backgroundHeight) / 2;
        RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
        if (this.isPointWithinBounds(275, 0, 20, 20, (double) mouseX, (double) mouseY)) {
            this.drawTexture(matrices, i + 275, j, 60, 0, 20, 20);
        } else
            this.drawTexture(matrices, i + 275, j, 40, 0, 20, 20);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        VillagerData villagerData = null;
        int i = 0;
        if (questScreenHandler.offerer instanceof VillagerEntity) {
            villagerData = ((VillagerEntity) questScreenHandler.offerer).getVillagerData();
            i = villagerData.getLevel();
        }
        Text title = villagerData != null ? Text.of(WordUtils.capitalize(villagerData.getProfession().getId())) : questScreenHandler.offerer.getName();
        if (i > 0 && i <= 5) {
            Text text = title.shallowCopy().append(" - ").append((Text) (new TranslatableText("merchant.level." + i)));
            int j = this.textRenderer.getWidth(text);
            int k = 49 + this.backgroundWidth / 2 - j / 2;
            this.textRenderer.draw(matrices, (Text) text, (float) k, 6.0F, 4210752);
        } else {
            this.textRenderer.draw(matrices, title, (float) (49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(title) / 2), 6.0F, 4210752);
        }
        int l = this.textRenderer.getWidth(new TranslatableText("merchant.quests"));
        this.textRenderer.draw(matrices, new TranslatableText("merchant.quests"), (float) (5 - l / 2 + 48), 6.0F, 4210752);

    }

    private void renderScrollbar(MatrixStack matrices, int x, int y, List<Integer> questIdList) {
        int i = questIdList.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            drawTexture(matrices, x + 94, y + 18 + m, this.getZOffset(), 244.0F, 0.0F, 6, 27, 256, 256);
        } else {
            drawTexture(matrices, x + 94, y + 18, this.getZOffset(), 250.0F, 0.0F, 6, 27, 256, 256);
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int i = questScreenHandler.questIdList.size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = (int) ((double) this.indexStartOffset - amount);
            this.indexStartOffset = MathHelper.clamp((int) this.indexStartOffset, (int) 0, (int) j);
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i = questScreenHandler.questIdList.size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float) mouseY - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.indexStartOffset = MathHelper.clamp((int) ((int) f), (int) 0, (int) l);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        if (this.canScroll(questScreenHandler.questIdList.size()) && mouseX > (double) (i + 94) && mouseX < (double) (i + 94 + 6) && mouseY > (double) (j + 18)
                && mouseY <= (double) (j + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        // Set villager trade screen
        if (this.isPointWithinBounds(275, 0, 20, 20, (double) mouseX, (double) mouseY)) {
            // this.onClose(); = brigher background for a mili second
            QuestClientPacket.writeC2STradePacket(this.questScreenHandler.offerer, (int) this.client.mouse.getX(), (int) this.client.mouse.getY());
            closedToTradeScreen = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (!closedToTradeScreen) {
            questScreenHandler.offerer.setCurrentCustomer((PlayerEntity) null);
            QuestClientPacket.writeC2SClosePacket(questScreenHandler.offerer);
        }
        super.removed();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        List<Integer> questIds = questScreenHandler.questIdList;
        if (!questIds.isEmpty()) {
            int i = (this.width - this.backgroundWidth) / 2;
            int j = (this.height - this.backgroundHeight) / 2;
            int k = j + 17;
            int l = i + 10;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
            this.renderScrollbar(matrices, i, j, questIds);
            if (this.selectedQuest != null)
                this.renderSelectedQuest(matrices, i, j);
            else
                acceptButton.visible = false;
            int m = 0;
            Iterator<Integer> var11 = questIds.iterator();
            while (true) {
                Quest quest;
                while (var11.hasNext()) {
                    quest = new Quest(var11.next());
                    if (this.canScroll(questIds.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                        ++m;
                    } else {
                        this.itemRenderer.zOffset = 100.0F;
                        int n = k + 2;
                        this.renderQuestItems(matrices, quest.getQuestTypeStack(), l, n);
                        // 10 width
                        float scaling = quest.getTitle().length() > 10 ? 10F / quest.getTitle().length() : 1.0F;
                        DrawableExtension.draw(matrices, textRenderer, quest.getTitle(), i + 30, n + 5, 0xE0E0E0, scaling);

                        this.itemRenderer.zOffset = 0.0F;
                        k += 20;
                        ++m;
                    }
                }

                QuestScreen.WidgetButtonPage[] var19 = this.quests;
                int var20 = var19.length;

                for (int var21 = 0; var21 < var20; ++var21) {
                    QuestScreen.WidgetButtonPage widgetButtonPage = var19[var21];
                    widgetButtonPage.visible = widgetButtonPage.index < questIds.size();
                }

                RenderSystem.enableDepthTest();
                break;
            }

            this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        }
    }

    private void renderQuestItems(MatrixStack matrices, ItemStack itemStack, int x, int y) {
        this.itemRenderer.renderInGui(itemStack, x, y);
    }

    private void renderSelectedQuest(MatrixStack matrices, int width, int hight) {
        boolean isBigDescription = textRenderer.getWidth(selectedQuest.getDescription()) > 570;
        // Draw title
        DrawableExtension.drawCenteredText(matrices, textRenderer, selectedQuest.getTitle(), width + 187, hight + 22 + (isBigDescription ? -1 : 0), 0xE0E0E0,
                1.1F + (isBigDescription ? -0.1F : 0.0F));

        // Draw quest text
        String[] string = selectedQuest.getDescription().split(" ");
        String stringCollector = "";
        List<String> list = new ArrayList<>();
        for (int u = 0; u < string.length; u++) {
            if (textRenderer.getWidth(stringCollector) < (isBigDescription ? 280 : 190)
                    && textRenderer.getWidth(stringCollector) + textRenderer.getWidth(string[u]) <= (isBigDescription ? 280 : 190)) {
                stringCollector = stringCollector + " " + string[u];
                if (u == string.length - 1)
                    list.add(stringCollector);
            } else {
                list.add(stringCollector);
                stringCollector = string[u];
                if (u == string.length - 1)
                    list.add(stringCollector);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            DrawableExtension.drawCenteredText(matrices, textRenderer, list.get(i), width + 187, hight + 32 + i * 7 + (isBigDescription ? -4 : 0), 0xE0E0E0,
                    0.8F + (isBigDescription ? -0.25F : 0.0F));
        }

        // Draw task
        DrawableExtension.draw(matrices, textRenderer, "Quest Task" + (selectedQuest.getStringTasks().length > 1 ? "s" : ""), width + 112, hight + 59, 0xE0E0E0, 0.9F);
        for (int u = 0; u < selectedQuest.getStringTasks().length; u++) {
            DrawableExtension.draw(matrices, textRenderer, selectedQuest.getStringTasks()[u], width + 115, hight + 67 + u * 7, 0xE0E0E0, 0.78F);
            DrawableExtension.renderQuestItems(matrices, client.getBufferBuilders().getEntityVertexConsumers(), itemRenderer, selectedQuest.getTaskStack(u),
                    (double) (100 + textRenderer.getWidth(selectedQuest.getStringTasks()[u]) * 0.795D), (double) (-44 - u * 7), 0.4F);

        }

        // Draw reward
        DrawableExtension.draw(matrices, textRenderer, "Quest Reward" + (selectedQuest.getStringRewards().length > 1 ? "s" : ""), width + 112, hight + 99, 0xE0E0E0, 0.9F);
        for (int u = 0; u < selectedQuest.getStringRewards().length; u++) {
            DrawableExtension.draw(matrices, textRenderer, selectedQuest.getStringRewards()[u], width + 115, hight + 107 + u * 7, 0xE0E0E0, 0.78F);
            DrawableExtension.renderQuestItems(matrices, client.getBufferBuilders().getEntityVertexConsumers(), itemRenderer, selectedQuest.getRewardStack(u),
                    (double) (100 + textRenderer.getWidth(selectedQuest.getStringRewards()[u]) * 0.795D), (double) (-84 - u * 7), 0.4F);
        }
        acceptButton.visible = true;
    }

    private class WidgetButtonPage extends ButtonWidget {
        final int index;

        public WidgetButtonPage(int x, int y, int index, ButtonWidget.PressAction onPress) {
            super(x, y, 89, 20, LiteralText.EMPTY, onPress);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        // @Override
        // public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        // super.renderTooltip(matrices, mouseX, mouseY);
        // }
    }

    private class AcceptButton extends ButtonWidget {
        public boolean unuseable = false;

        public AcceptButton(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 89, 20, LiteralText.EMPTY, onPress);
            this.visible = false;
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            super.renderTooltip(matrices, mouseX, mouseY);
            if (unuseable) {
                client.currentScreen.renderTooltip(matrices, Text.of("Unusable"), x, y);
            }
        }

    }
}