package net.villagerquests.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.network.QuestClientPacket;
import net.villagerquests.util.DrawableExtension;

@Environment(EnvType.CLIENT)
public class QuestScreen extends HandledScreen<QuestScreenHandler> implements Tab {

    public static final Identifier GUI_ICONS = new Identifier("villagerquests:textures/gui/screen_and_icons.png");
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

    public QuestScreen(QuestScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.playerEntity = playerInventory.player;
        this.acceptedQuestIdList = ((PlayerAccessor) this.playerEntity).getPlayerQuestIdList();
    }

    @Override
    public Class<?> getParentScreenClass() {
        return MerchantScreen.class;
    }

    @Override
    public void init() {
        this.backgroundWidth = 276;
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int k = j + 16 + 2;
        this.selectedQuest = null;
        this.acceptButton = this.addDrawableChild(new QuestScreen.AcceptButton(i + 161, j + 139, (button) -> {
            if (button instanceof QuestScreen.AcceptButton) {
                if (this.acceptedQuestIdList.contains(this.selectedQuest.getQuestId()) && this.selectedQuest.canCompleteQuest(this.playerEntity))
                    this.completeQuest();
                else
                    this.acceptQuest();
            }
        }));
        this.acceptButton.setMessage(Text.translatable("text.villagerquests.acceptButton"));

        this.declineButton = this.addDrawableChild(new QuestScreen.DeclineButton(i + 247, j + 140, (button) -> {
            if (button instanceof QuestScreen.DeclineButton) {
                this.declineQuest();
            }
        }));

        for (int l = 0; l < 7; ++l) {
            this.quests[l] = this.addDrawableChild(new QuestScreen.WidgetButtonPage(i + 5, k, l, this.handler.questIdList.size() > l ? this.handler.questIdList.get(l) : -1, (button) -> {
                if (button instanceof QuestScreen.WidgetButtonPage) {
                    this.selectedIndex = ((QuestScreen.WidgetButtonPage) button).getIndex() + this.indexStartOffset;
                    this.selectedQuest = new Quest(this.handler.questIdList.get(this.selectedIndex));
                    this.acceptButton.visible = true;
                    if (this.acceptedQuestIdList.contains(this.selectedQuest.getQuestId())) {
                        if (((PlayerAccessor) this.playerEntity).isOriginalQuestGiver(this.handler.offerer.getUuid(), this.selectedQuest.getQuestId())) {
                            this.acceptButton.setMessage(Text.translatable("text.villagerquests.completeButton"));
                            this.declineButton.visible = true;
                            if (this.selectedQuest.canCompleteQuest(this.playerEntity)) {
                                this.acceptButton.active = true;
                            } else {
                                this.acceptButton.active = false;
                            }
                        } else {
                            this.acceptButton.active = false;
                        }
                    } else {
                        if (((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().contains(this.selectedQuest.getQuestId()) && !((PlayerAccessor) this.playerEntity)
                                .getPlayerQuestRefreshTimerList().get(((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().indexOf(this.selectedQuest.getQuestId())).equals(-1)) {
                            this.acceptButton.active = false;
                        } else {
                            this.acceptButton.active = true;
                        }
                        this.acceptButton.setMessage(Text.translatable("text.villagerquests.acceptButton"));
                        this.declineButton.visible = false;

                    }
                }
            }));
            if (this.handler.questIdList.size() > l) {
                if (((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().contains(this.handler.questIdList.get(l)) && !((PlayerAccessor) this.playerEntity)
                        .getPlayerQuestRefreshTimerList().get(((PlayerAccessor) this.playerEntity).getPlayerFinishedQuestIdList().indexOf(this.handler.questIdList.get(l))).equals(-1)) {
                    this.quests[l].active = false;
                }
            }
            k += 20;
        }
    }

    private void acceptQuest() {
        acceptButton.setMessage(Text.translatable("text.villagerquests.completeButton"));
        QuestClientPacket.acceptMerchantQuestC2SPacket(client.player, this.selectedQuest.getQuestId(), this.handler.offerer.getUuid(), this.handler.offerer.getId());
        if (this.selectedQuest.canCompleteQuest(this.playerEntity)) {
            this.acceptButton.active = true;
        } else {
            this.acceptButton.active = false;
        }
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
                .equals(-1)) {
            this.quests[selectedIndex].active = false;
        } else {
            this.quests[selectedIndex].visible = false;
            this.handler.questIdList.remove(selectedIndex);
        }
        this.acceptButton.visible = false;
        this.declineButton.visible = false;
        this.selectedQuest = null;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(GUI_ICONS, i, j, 0, 32, 276, 166, 512, 512);

        if (this.selectedQuest != null) {
            context.drawTexture(GUI_ICONS, i + 107, j + 17, 276, 32, 162, 142, 512, 512);
        }

    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // super.drawForeground(context, mouseX, mouseY);
        VillagerData villagerData = null;
        int i = 0;
        if (this.handler.offerer instanceof VillagerEntity) {
            villagerData = ((VillagerEntity) this.handler.offerer).getVillagerData();
            i = villagerData.getLevel();
        }
        // Old usage: villagerData != null ? Text.of(StringUtils.capitalize(villagerData.getProfession().getId())) :
        Text title = this.handler.offerer.getName();
        if (i > 0 && i <= 5) {
            Text text = title.copy().append(" - ").append((Text) (Text.translatable("merchant.level." + i)));
            int j = this.textRenderer.getWidth(text);
            int k = 49 + this.backgroundWidth / 2 - j / 2;
            context.drawText(this.textRenderer, text, k, 6, 4210752, false);
        } else {
            context.drawText(this.textRenderer, title, 49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2, 6, 4210752, false);
        }
        int l = this.textRenderer.getWidth(Text.translatable("merchant.quests"));
        context.drawText(this.textRenderer, Text.translatable("merchant.quests"), 5 - l / 2 + 48, 6, 4210752, false);
    }

    private void renderScrollbar(DrawContext context, int x, int y, List<Integer> questIdList) {
        int i = questIdList.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            context.drawTexture(GUI_ICONS, x + 94, y + 18 + m, 244, 0, 6, 27, 512, 512);
        } else {
            context.drawTexture(GUI_ICONS, x + 94, y + 18, 250, 0, 6, 27, 512, 512);
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setClosedToTradeScreen() {
        this.closedToTradeScreen = true;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        List<Integer> questIds = this.handler.questIdList;
        if (!questIds.isEmpty()) {
            int i = (this.width - this.backgroundWidth) / 2;
            int j = (this.height - this.backgroundHeight) / 2;
            int k = j + 17;
            int l = i + 10;
            this.renderScrollbar(context, i, j, questIds);
            if (this.selectedQuest != null) {
                this.renderSelectedQuest(context, i, j);
            } else {
                acceptButton.visible = false;
            }
            int m = 0;
            Iterator<Integer> var11 = questIds.iterator();
            while (true) {
                Quest quest;
                while (var11.hasNext()) {
                    quest = new Quest(var11.next());
                    if (this.canScroll(questIds.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                        ++m;
                    } else {
                        // this.itemRenderer.zOffset = 100.0F;
                        int n = k + 2;
                        context.drawItem(quest.getQuestTypeStack(), l, n);
                        // this.renderQuestItems(matrices, quest.getQuestTypeStack(), l, n);
                        // 10 width
                        float scaling = quest.getTitle().length() > 10 ? 10F / quest.getTitle().length() : 1.0F;
                        DrawableExtension.drawText(context, textRenderer, quest.getTitle(), i + 30, n + 5, VillagerQuestsMain.CONFIG.questTabTitleColor, scaling);
                        // this.itemRenderer.zOffset = 0.0F;
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

            this.drawMouseoverTooltip(context, mouseX, mouseY);
        }
    }

    // private void renderQuestItems(MatrixStack matrices, ItemStack itemStack, int x, int y) {
    // this.itemRenderer.renderInGui(itemStack, x, y);
    // }

    private void renderSelectedQuest(DrawContext context, int width, int hight) {
        // VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        boolean isBigDescription = textRenderer.getWidth(selectedQuest.getDescription()) > 570;
        // Draw title
        DrawableExtension.drawCenteredTextWithShadow(context, textRenderer, selectedQuest.getTitle(), width + 187, hight + 22 + (isBigDescription ? -1 : 0), VillagerQuestsMain.CONFIG.titleColor,
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
            DrawableExtension.drawCenteredText(context, textRenderer, list.get(i), width + 187, hight + 32 + i * 7 + (isBigDescription ? -4 : 0), VillagerQuestsMain.CONFIG.descriptionColor,
                    0.8F + (isBigDescription ? -0.25F : 0.0F));
        }

        // Draw task
        DrawableExtension.drawText(context, textRenderer, Text.translatable("text.villagerquests.questTask").getString(), width + 112, hight + 59, VillagerQuestsMain.CONFIG.taskHeaderColor, 0.9F);
        // Draw time of task
        if (selectedQuest.getQuestTimer() != -1)
            DrawableExtension.drawText(context, textRenderer, Text.translatable("text.villagerquests.timer").getString() + selectedQuest.getTimerString(),
                    width + 215 + (selectedQuest.getQuestTimer() / 20 >= 3600 ? -20 : 0), hight + 59, VillagerQuestsMain.CONFIG.taskHeaderColor, 0.9F);

        for (int u = 0; u < selectedQuest.getStringTasks().length; u++) {
            String task = selectedQuest.getStringTasks()[u];
            int addition = 0;
            if (textRenderer.getWidth(task) > 191) {
                addition += 7;
                String[] longStrings = task.split(" ");
                String newTask = "";
                for (int i = 0; i < longStrings.length; i++) {
                    if (textRenderer.getWidth(newTask + longStrings[i] + " ") < 191) {
                        if (i + 1 == longStrings.length) {
                            newTask = newTask + longStrings[i];
                            DrawableExtension.drawText(context, textRenderer, newTask, width + 115 + textRenderer.getWidth(u + " "), hight + 67 + u * 7 + addition,
                                    VillagerQuestsMain.CONFIG.taskColor, 0.78F);
                            DrawableExtension.renderQuestItems(context, selectedQuest.getTaskStack(u), (double) (250 + (textRenderer.getWidth(newTask) + textRenderer.getWidth(u + " ")) * 2D),
                                    (double) (-59 - u * 18 + addition) + this.indexStartOffset * -50, 0.4F);
                        } else {
                            newTask = newTask + longStrings[i] + " ";
                        }
                    } else {

                        DrawableExtension.drawText(context, textRenderer, newTask, width + 115, hight + 67 + u * 7, VillagerQuestsMain.CONFIG.taskColor, 0.78F);
                        if (i + 1 == longStrings.length) {
                            newTask = longStrings[i];
                            DrawableExtension.drawText(context, textRenderer, newTask, width + 115 + textRenderer.getWidth(u + " "), hight + 67 + u * 7 + addition,
                                    VillagerQuestsMain.CONFIG.taskColor, 0.78F);
                            DrawableExtension.renderQuestItems(context, selectedQuest.getTaskStack(u), (double) (250 + (textRenderer.getWidth(newTask) + textRenderer.getWidth(u + " ")) * 2D),
                                    (double) (-59 - u * 18 + addition) + this.indexStartOffset * -50, 0.4F);
                        } else {
                            newTask = longStrings[i] + " ";
                        }
                    }
                }
            } else {
                DrawableExtension.drawText(context, textRenderer, task, width + 115, hight + 67 + u * 7 + addition, VillagerQuestsMain.CONFIG.taskColor, 0.78F);
                DrawableExtension.renderQuestItems(context, selectedQuest.getTaskStack(u), (double) (250 + textRenderer.getWidth(task) * 2D), (double) (-59 - u * 18) + this.indexStartOffset * -50,
                        0.4F);
            }

        }

        // Draw reward
        DrawableExtension.drawText(context, textRenderer, Text.translatable("text.villagerquests.questReward").getString(), width + 112, hight + 99, VillagerQuestsMain.CONFIG.rewardHeaderColor,
                0.9F);
        for (int u = 0; u < selectedQuest.getStringRewards().length; u++) {
            DrawableExtension.drawText(context, textRenderer, selectedQuest.getStringRewards()[u], width + 115, hight + 107 + u * 7, VillagerQuestsMain.CONFIG.rewardColor, 0.78F);
            DrawableExtension.renderQuestItems(context, selectedQuest.getRewardStack(u), (double) (250 + textRenderer.getWidth(selectedQuest.getStringRewards()[u]) * 2D),
                    (double) (-159 - u * 18) + this.indexStartOffset * -50, 0.4F);

            // System.out.println((double) (-159 - u * 18) + this.indexStartOffset * -50 + " : " + this.indexStartOffset + " : " + u);
        }
    }

    private class WidgetButtonPage extends ButtonWidget {

        final int index;
        final int questId;

        public WidgetButtonPage(int x, int y, int index, int questId, PressAction onPress) {
            super(x, y, 89, 20, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.index = index;
            this.questId = questId;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public Tooltip getTooltip() {
            if (((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList().contains(questId)) {
                int refreshTicks = ((PlayerAccessor) playerEntity).getPlayerQuestRefreshTimerList().get(((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList().indexOf(questId));
                if (refreshTicks != -1) {
                    refreshTicks = refreshTicks / 20;
                    String string;
                    if (refreshTicks >= 3600) {
                        string = String.format("%02d:%02d:%02d", refreshTicks / 3600, (refreshTicks % 3600) / 60, (refreshTicks % 60));
                    } else {
                        string = String.format("%02d:%02d", (refreshTicks % 3600) / 60, (refreshTicks % 60));
                    }
                    return Tooltip.of(Text.of(string));
                }
            }
            return super.getTooltip();
        }

    }

    private class AcceptButton extends ButtonWidget {

        public AcceptButton(int x, int y, PressAction onPress) {
            super(x, y, 55, 17, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.visible = false;
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            context.drawTexture(GUI_ICONS, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0, 205 + this.getTextureY(), this.getWidth(), this.getHeight(), 512, 512);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int i = this.active ? 0xFFFFFF : 0xA0A0A0;
            this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0f) << 24);
        }

        private int getTextureY() {
            int i = 1;
            if (!this.active) {
                i = 0;
            } else if (this.isSelected()) {
                i = 2;
            }
            return i * 17;
        }

    }

    private class DeclineButton extends ButtonWidget {

        public DeclineButton(int x, int y, PressAction onPress) {
            super(x, y, 16, 15, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.visible = false;
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            context.drawTexture(GUI_ICONS, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 64, 205 + (this.isHovered() ? 15 : 0), this.getWidth(), this.getHeight(), 512, 512);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        @Override
        public Tooltip getTooltip() {
            return Tooltip.of(Text.translatable("text.villagerquests.decline"));
        }

    }
}