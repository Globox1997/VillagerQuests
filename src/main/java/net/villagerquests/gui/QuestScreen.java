package net.villagerquests.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestClientPacket;
import net.villagerquests.util.DrawableExtension;

@Environment(EnvType.CLIENT)
public class QuestScreen extends CottonInventoryScreen<QuestScreenHandler> {
    private int indexStartOffset;
    private boolean scrolling;
    private int selectedIndex;
    private final QuestScreen.WidgetButtonPage[] quests = new QuestScreen.WidgetButtonPage[7];
    private Quest selectedQuest;
    private QuestScreen.AcceptButton acceptButton;
    private QuestScreen.DeclineButton declineButton;
    private List<Integer> acceptedQuestIdList;
    private boolean closedToTradeScreen = false;
    private final PlayerEntity playerEntity;

    public QuestScreen(QuestScreenHandler handler, PlayerEntity player, Text title) {
        super(handler, player, title);
        this.playerEntity = player;
        acceptedQuestIdList = ((PlayerAccessor) player).getPlayerQuestIdList();
    }

    @Override
    public void init() {
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int k = j + 16 + 2;
        this.selectedQuest = null;
        this.acceptButton = (QuestScreen.AcceptButton) this.addDrawableChild(new QuestScreen.AcceptButton(i + 161, j + 139, (button) -> {
            if (button instanceof QuestScreen.AcceptButton) {
                if (this.acceptedQuestIdList.contains(this.selectedQuest.getQuestId()) && this.selectedQuest.canCompleteQuest(this.playerEntity))
                    this.completeQuest();
                else
                    this.acceptQuest();
            }
        }));
        this.acceptButton.setMessage(new TranslatableText("text.villagerquests.acceptButton"));

        this.declineButton = (QuestScreen.DeclineButton) this.addDrawableChild(new QuestScreen.DeclineButton(i + 247, j + 140, (button) -> {
            if (button instanceof QuestScreen.DeclineButton) {
                this.declineQuest();
            }
        }));

        for (int l = 0; l < 7; ++l) {
            this.quests[l] = (QuestScreen.WidgetButtonPage) this
                    .addDrawableChild(new QuestScreen.WidgetButtonPage(i + 5, k, l, this.handler.questIdList.size() > l ? this.handler.questIdList.get(l) : -1, (button) -> {
                        if (button instanceof QuestScreen.WidgetButtonPage) {
                            this.selectedIndex = ((QuestScreen.WidgetButtonPage) button).getIndex() + this.indexStartOffset;
                            this.selectedQuest = new Quest(this.handler.questIdList.get(this.selectedIndex));
                            this.acceptButton.visible = true;
                            if (this.acceptedQuestIdList.contains(this.selectedQuest.getQuestId())) {
                                if (((PlayerAccessor) this.playerEntity).isOriginalQuestGiver(this.handler.offerer.getUuid(), this.selectedQuest.getQuestId())) {
                                    this.acceptButton.setMessage(new TranslatableText("text.villagerquests.completeButton"));
                                    this.declineButton.visible = true;
                                    if (this.selectedQuest.canCompleteQuest(this.playerEntity))
                                        this.acceptButton.active = true;
                                    else
                                        this.acceptButton.active = false;
                                } else
                                    this.acceptButton.active = false;

                            } else {
                                if (((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().contains(this.selectedQuest.getQuestId())
                                        && !((PlayerAccessor) this.playerEntity).getPlayerQuestRefreshTimerList()
                                                .get(((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().indexOf(this.selectedQuest.getQuestId())).equals(-1)) {
                                    this.acceptButton.active = false;
                                } else
                                    this.acceptButton.active = true;
                                this.acceptButton.setMessage(new TranslatableText("text.villagerquests.acceptButton"));
                                this.declineButton.visible = false;

                            }
                        }
                    }));
            if (this.handler.questIdList.size() > l)
                if (((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().contains(this.handler.questIdList.get(l)) && !((PlayerAccessor) this.playerEntity)
                        .getPlayerQuestRefreshTimerList().get(((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().indexOf(this.handler.questIdList.get(l))).equals(-1))
                    this.quests[l].active = false;
            k += 20;
        }
    }

    private void acceptQuest() {
        acceptButton.setMessage(new TranslatableText("text.villagerquests.completeButton"));
        QuestClientPacket.acceptMerchantQuestC2SPacket(client.player, this.selectedQuest.getQuestId(), this.handler.offerer.getUuid(), this.handler.offerer.getId());
        if (this.selectedQuest.canCompleteQuest(this.playerEntity))
            this.acceptButton.active = true;
        else
            this.acceptButton.active = false;
        this.declineButton.visible = true;
    }

    private void completeQuest() {
        ((PlayerAccessor) this.playerEntity).finishPlayerQuest(this.selectedQuest.getQuestId());
        QuestClientPacket.writeC2SQuestCompletionPacket(this.selectedQuest.getQuestId(), this.handler.offerer.getId(), this.selectedQuest.getQuestLevel());
        this.removeQuest();
    }

    private void declineQuest() {
        ((PlayerAccessor) this.playerEntity).failPlayerQuest(this.selectedQuest.getQuestId(), 3);
        QuestClientPacket.writeC2SQuestDeclinePacket(this.selectedQuest.getQuestId(), 3, this.handler.offerer.getId());
        this.removeQuest();
    }

    private void removeQuest() {
        if (!((PlayerAccessor) this.playerEntity).getPlayerQuestRefreshTimerList().get(((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().indexOf(this.selectedQuest.getQuestId()))
                .equals(-1))
            this.quests[selectedIndex].active = false;
        else {
            this.quests[selectedIndex].visible = false;
            this.handler.questIdList.remove(selectedIndex);
        }
        this.acceptButton.visible = false;
        this.declineButton.visible = false;
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

        drawTexture(matrices, i, j, this.getZOffset(), 0.0F, 32.0F, 97, 142, 512, 512);
        if (this.selectedQuest == null)
            drawTexture(matrices, i + 103, j, this.getZOffset(), 97.0F, 32.0F, 159, 142, 512, 512);
        else
            drawTexture(matrices, i + 103, j, this.getZOffset(), 256.0F, 32.0F, 159, 142, 512, 512);

        // Draw trade screen button
        i = (this.width - this.backgroundWidth) / 2;
        j = (this.height - this.backgroundHeight) / 2;

        if (this.isPointWithinBounds(276 + VillagerQuestsMain.CONFIG.xIconPosition, 0 + VillagerQuestsMain.CONFIG.yIconPosition, 20, 20, (double) mouseX, (double) mouseY)) {
            QuestScreen.drawTexture(matrices, i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 60, 0, 20, 20, 512, 512);
        } else
            QuestScreen.drawTexture(matrices, i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 40, 0, 20, 20, 512, 512);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        VillagerData villagerData = null;
        int i = 0;
        if (this.handler.offerer instanceof VillagerEntity) {
            villagerData = ((VillagerEntity) this.handler.offerer).getVillagerData();
            i = villagerData.getLevel();
        }
        // Old usage: villagerData != null ? Text.of(StringUtils.capitalize(villagerData.getProfession().getId())) :
        Text title = this.handler.offerer.getName();
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
            drawTexture(matrices, x + 94, y + 18 + m, this.getZOffset(), 244.0F, 0.0F, 6, 27, 512, 512);
        } else {
            drawTexture(matrices, x + 94, y + 18, this.getZOffset(), 250.0F, 0.0F, 6, 27, 512, 512);
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int i = this.handler.questIdList.size();
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
        int i = this.handler.questIdList.size();
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
        if (this.canScroll(this.handler.questIdList.size()) && mouseX > (double) (i + 94) && mouseX < (double) (i + 94 + 6) && mouseY > (double) (j + 18) && mouseY <= (double) (j + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        // Set villager trade screen
        if (this.isPointWithinBounds(276 + VillagerQuestsMain.CONFIG.xIconPosition, 0 + VillagerQuestsMain.CONFIG.yIconPosition, 20, 20, (double) mouseX, (double) mouseY)) {
            // this.onClose(); = bright background for a mili second
            QuestClientPacket.writeC2STradePacket(this.handler.offerer, (int) this.client.mouse.getX(), (int) this.client.mouse.getY());
            closedToTradeScreen = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (!closedToTradeScreen) {
            this.handler.offerer.setCustomer(null);
            QuestClientPacket.writeC2SClosePacket(this.handler.offerer);
        }
        super.removed();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        List<Integer> questIds = this.handler.questIdList;
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
                        DrawableExtension.draw(matrices, textRenderer, quest.getTitle(), i + 30, n + 5, VillagerQuestsMain.CONFIG.questTabTitleColor, scaling);
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
        DrawableExtension.drawCenteredText(matrices, textRenderer, selectedQuest.getTitle(), width + 187, hight + 22 + (isBigDescription ? -1 : 0), VillagerQuestsMain.CONFIG.titleColor,
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
            DrawableExtension.drawCenteredText(matrices, textRenderer, list.get(i), width + 187, hight + 32 + i * 7 + (isBigDescription ? -4 : 0), VillagerQuestsMain.CONFIG.descriptionColor,
                    0.8F + (isBigDescription ? -0.25F : 0.0F));
        }

        // Draw task
        DrawableExtension.draw(matrices, textRenderer, new TranslatableText("text.villagerquests.questTask").getString(), width + 112, hight + 59, VillagerQuestsMain.CONFIG.taskHeaderColor, 0.9F);
        // Draw time of task
        if (selectedQuest.getQuestTimer() != -1)
            DrawableExtension.draw(matrices, textRenderer, new TranslatableText("text.villagerquests.timer").getString() + selectedQuest.getTimerString(),
                    width + 215 + (selectedQuest.getQuestTimer() / 20 >= 3600 ? -20 : 0), hight + 59, VillagerQuestsMain.CONFIG.taskHeaderColor, 0.9F);

        for (int u = 0; u < selectedQuest.getStringTasks().length; u++) {
            DrawableExtension.draw(matrices, textRenderer, selectedQuest.getStringTasks()[u], width + 115, hight + 67 + u * 7, VillagerQuestsMain.CONFIG.taskColor, 0.78F);
            DrawableExtension.renderQuestItems(matrices, client.getBufferBuilders().getEntityVertexConsumers(), itemRenderer, selectedQuest.getTaskStack(u),
                    (double) (100 + textRenderer.getWidth(selectedQuest.getStringTasks()[u]) * 0.795D), (double) (-44 - u * 7), 0.4F);

        }

        // Draw reward
        DrawableExtension.draw(matrices, textRenderer, new TranslatableText("text.villagerquests.questReward").getString(), width + 112, hight + 99, VillagerQuestsMain.CONFIG.rewardHeaderColor,
                0.9F);
        for (int u = 0; u < selectedQuest.getStringRewards().length; u++) {
            DrawableExtension.draw(matrices, textRenderer, selectedQuest.getStringRewards()[u], width + 115, hight + 107 + u * 7, VillagerQuestsMain.CONFIG.rewardColor, 0.78F);
            DrawableExtension.renderQuestItems(matrices, client.getBufferBuilders().getEntityVertexConsumers(), itemRenderer, selectedQuest.getRewardStack(u),
                    (double) (100 + textRenderer.getWidth(selectedQuest.getStringRewards()[u]) * 0.795D), (double) (-84 - u * 7), 0.4F);
        }
    }

    private class WidgetButtonPage extends ButtonWidget {
        final int index;
        final int questId;

        public WidgetButtonPage(int x, int y, int index, int questId, ButtonWidget.PressAction onPress) {
            super(x, y, 89, 20, LiteralText.EMPTY, onPress);
            this.index = index;
            this.questId = questId;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            super.renderTooltip(matrices, mouseX, mouseY);
            if (((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList().contains(questId)) {
                int refreshTicks = ((PlayerAccessor) playerEntity).getPlayerQuestRefreshTimerList().get(((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList().indexOf(questId));
                if (refreshTicks != -1) {
                    refreshTicks = refreshTicks / 20;
                    String string;
                    if (refreshTicks >= 3600)
                        string = String.format("%02d:%02d:%02d", refreshTicks / 3600, (refreshTicks % 3600) / 60, (refreshTicks % 60));
                    else
                        string = String.format("%02d:%02d", (refreshTicks % 3600) / 60, (refreshTicks % 60));
                    client.currentScreen.renderTooltip(matrices, new TranslatableText("text.villagerquests.refreshing", string), mouseX, mouseY);
                }
            }
        }

    }

    private class AcceptButton extends ButtonWidget {

        public AcceptButton(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 55, 17, LiteralText.EMPTY, onPress);
            this.visible = false;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            TextRenderer textRenderer = minecraftClient.textRenderer;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHovered());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            WidgetButtonPage.drawTexture(matrices, this.x, this.y, 0, 176 + i * 17, 55, this.height, 512, 512);
            int j = this.active ? 16777215 : 10526880;
            drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 6) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }

    }

    private class DeclineButton extends ButtonWidget {

        public DeclineButton(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 16, 15, LiteralText.EMPTY, onPress);
            this.visible = false;
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            super.renderTooltip(matrices, mouseX, mouseY);
            client.currentScreen.renderTooltip(matrices, new TranslatableText("text.villagerquests.decline"), mouseX, mouseY);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            WidgetButtonPage.drawTexture(matrices, this.x, this.y, 64, 176 + (this.isHovered() ? 15 : 0), this.width, this.height, 512, 512);
            if (this.isHovered()) {
                this.renderTooltip(matrices, mouseX, mouseY);
            }
        }
    }
}