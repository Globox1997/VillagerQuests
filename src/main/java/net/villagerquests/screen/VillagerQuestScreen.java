package net.villagerquests.screen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.ClaimRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.init.RenderInit;
import net.villagerquests.network.QuestClientPacket;
import net.villagerquests.screen.widget.DescriptionWidget;

@Environment(EnvType.CLIENT)
public class VillagerQuestScreen extends HandledScreen<VillagerQuestScreenHandler> implements Tab {

    private int indexStartOffset;
    private boolean scrolling;
    private int selectedIndex;
    private boolean showTabs;

    private Quest selectedQuest;

    private final VillagerQuestScreen.WidgetButtonPage[] quests = new VillagerQuestScreen.WidgetButtonPage[7];
    private VillagerQuestScreen.AcceptButton acceptButton;
    private VillagerQuestScreen.DeclineButton declineButton;

    private List<Quest> questList = new ArrayList<Quest>();
    private List<Text> descriptionList = new ArrayList<Text>();
    private TeamData teamData;

    public VillagerQuestScreen(VillagerQuestScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 276;
        teamData = ClientQuestFile.INSTANCE.selfTeamData;
        showTabs = true;

        for (int i = 0; i < ClientQuestFile.INSTANCE.getAllChapters().size(); i++) {
            List<Quest> quests = ClientQuestFile.INSTANCE.getAllChapters().get(i).getQuests();
            for (int u = 0; u < quests.size(); u++) {
                Quest quest = quests.get(u);
                if (((QuestAccessor) (Object) quest).isVillagerQuest() && ((QuestAccessor) (Object) quest).getVillagerQuestUuid().equals(handler.offerer.getUuid())
                        && ((QuestAccessor) (Object) quest).isQuestVisible(teamData)) {
                    if (teamData.isCompleted(quest) && !quest.hasUnclaimedRewardsRaw(teamData, playerInventory.player.getUuid()) && !quest.canBeRepeated()) {
                        continue;
                    }
                    this.questList.add(quest);
                }
            }
        }
    }

    @Override
    public Class<?> getParentScreenClass() {
        return MerchantScreen.class;
    }

    @Override
    public void init() {
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int k = j + 16 + 2;
        this.selectedQuest = null;
        this.descriptionList.clear();
        this.acceptButton = this.addDrawableChild(new VillagerQuestScreen.AcceptButton(i + 161, j + 139, (button) -> {
            if (this.selectedQuest != null) {
                if (this.selectedQuest.isCompletedRaw(this.teamData)) {
                    QuestClientPacket.writeC2SCompleteQuestPacket(this.selectedQuest.id);
                    Iterator<Reward> iterator = this.selectedQuest.getRewards().iterator();
                    while (iterator.hasNext()) {
                        Reward reward = iterator.next();
                        new ClaimRewardMessage(reward.id, true).sendToServer();
                    }
                    removeQuest(this.selectedQuest);
                } else {
                    ((QuestAccessor) (Object) this.selectedQuest).setAccepted(true);
                    QuestClientPacket.writeC2SAcceptQuestPacket(this.selectedQuest.id, true);
                    this.acceptButton.active = false;
                    this.declineButton.active = true;
                    this.declineButton.visible = true;
                    this.acceptButton.setMessage(Text.translatable("screen.villagerquests.completeButton"));
                }
            }
        }));
        this.acceptButton.setMessage(Text.translatable("screen.villagerquests.acceptButton"));

        this.declineButton = this.addDrawableChild(new VillagerQuestScreen.DeclineButton(i + 247, j + 140, (button) -> {
            if (this.selectedQuest != null) {
                ((QuestAccessor) (Object) this.selectedQuest).setAccepted(false);
                QuestClientPacket.writeC2SAcceptQuestPacket(this.selectedQuest.id, false);
                this.declineButton.visible = false;
                this.acceptButton.visible = false;
                this.quests[selectedIndex].active = false;
                this.selectedQuest = null;
                this.descriptionList.clear();
            }
        }));

        for (int l = 0; l < 7; ++l) {
            this.quests[l] = this.addDrawableChild(new VillagerQuestScreen.WidgetButtonPage(i + 5, k, l, this.questList.size() > l ? this.questList.get(l) : null, (button) -> {
                if (button instanceof VillagerQuestScreen.WidgetButtonPage widgetButtonPage) {
                    this.selectedIndex = widgetButtonPage.getIndex() + this.indexStartOffset;
                    this.selectedQuest = this.questList.get(this.selectedIndex);
                    this.descriptionList.clear();
                    this.descriptionList.addAll(this.selectedQuest.getDescription());

                    if (this.selectedQuest.isCompletedRaw(this.teamData)) {
                        this.acceptButton.setMessage(Text.translatable("screen.villagerquests.completeButton"));
                        this.acceptButton.active = true;
                        this.declineButton.active = true;
                        this.declineButton.visible = true;
                    } else {
                        if (((QuestAccessor) (Object) this.selectedQuest).isAccepted()) {
                            this.acceptButton.setMessage(Text.translatable("screen.villagerquests.completeButton"));
                            this.acceptButton.active = false;

                            this.declineButton.active = true;
                            this.declineButton.visible = true;
                        } else {
                            this.acceptButton.setMessage(Text.translatable("screen.villagerquests.acceptButton"));
                            this.acceptButton.active = true;
                            this.declineButton.active = false;
                            this.declineButton.visible = false;
                        }

                    }
                    this.acceptButton.visible = true;
                }
            }));
            k += 20;
        }
        this.addDrawableChild(new DescriptionWidget(i + 111, j + 21, 154, 113, this.descriptionList, this.textRenderer));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, 512, 512);

        if (this.selectedQuest != null) {
            context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, i + 107, j + 17, 276, 0, 162, 142, 512, 512);
        }
    }

    private void removeQuest(Quest quest) {
        if (quest.canBeRepeated()) {
            this.quests[selectedIndex].active = false;
        } else {
            this.quests[selectedIndex].visible = false;
            this.questList.remove(selectedIndex);
        }
        this.acceptButton.visible = false;
        this.declineButton.visible = false;
        this.selectedQuest = null;
        this.descriptionList.clear();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        VillagerData villagerData = null;
        int i = 0;
        if (this.handler.offerer instanceof VillagerEntity) {
            villagerData = ((VillagerEntity) this.handler.offerer).getVillagerData();
            i = villagerData.getLevel();
        }
        Text title = this.handler.offerer.getName();
        if (i > 0 && i <= 5) {
            Text text = title.copy().append(" - ").append((Text) (Text.translatable("merchant.level." + i)));
            int j = this.textRenderer.getWidth(text);
            int k = 49 + this.backgroundWidth / 2 - j / 2;
            context.drawText(this.textRenderer, text, k, 6, 4210752, false);
        } else {
            context.drawText(this.textRenderer, title, 49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2, 6, 4210752, false);
        }
        int l = this.textRenderer.getWidth(Text.translatable("screen.villagerquests"));
        context.drawText(this.textRenderer, Text.translatable("screen.villagerquests"), 5 - l / 2 + 48, 6, 4210752, false);
    }

    private void renderScrollbar(DrawContext context, int x, int y, List<Quest> questList) {
        int i = questList.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, x + 94, y + 18 + m, 438, 0, 6, 27, 512, 512);
        } else {
            context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, x + 94, y + 18, 444, 0, 6, 27, 512, 512);
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int i = this.questList.size();
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
        int i = this.questList.size();
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
        if (this.canScroll(this.questList.size()) && mouseX > (double) (i + 94) && mouseX < (double) (i + 94 + 6) && mouseY > (double) (j + 18) && mouseY <= (double) (j + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        if (!this.questList.isEmpty()) {
            this.renderScrollbar(context, this.x, this.y, this.questList);

            int k = this.y + 19;
            int m = 0;
            Iterator<Quest> quests = this.questList.iterator();
            while (true) {
                while (quests.hasNext()) {
                    Quest quest = quests.next();
                    if (this.canScroll(this.questList.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                        ++m;
                    } else {
                        if (this.textRenderer.getWidth(quest.getTitle()) > 82) {
                            context.drawText(this.textRenderer, Text.literal(this.textRenderer.trimToWidth(quest.getTitle().getString(), 78) + "..").setStyle(quest.getTitle().getStyle()), this.x + 9,
                                    k + 5, 0xFFFFFF, false);
                            if (this.isPointWithinBounds(6, k - this.y, 87, 18, mouseX, mouseY)) {
                                context.drawTooltip(this.textRenderer, quest.getTitle(), mouseX, mouseY);
                            }
                        } else {
                            context.drawText(this.textRenderer, quest.getTitle(), this.x + 9, k + 5, 0xFFFFFF, false);
                        }
                        k += 20;
                        ++m;
                    }
                }

                VillagerQuestScreen.WidgetButtonPage[] buttonPages = this.quests;
                int buttonId = buttonPages.length;

                for (int u = 0; u < buttonId; ++u) {
                    VillagerQuestScreen.WidgetButtonPage widgetButtonPage = buttonPages[u];
                    widgetButtonPage.visible = widgetButtonPage.index < this.questList.size();
                }

                RenderSystem.enableDepthTest();
                break;
            }

            this.drawMouseoverTooltip(context, mouseX, mouseY);
        }
    }

    public void setShowTabs(boolean showTabs) {
        this.showTabs = showTabs;
    }

    public boolean showTabs() {
        return this.showTabs;
    }

    private class WidgetButtonPage extends ButtonWidget {

        final int index;
        final Quest quest;

        public WidgetButtonPage(int x, int y, int index, Quest quest, PressAction onPress) {
            super(x, y, 89, 20, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.index = index;
            this.quest = quest;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        // @Override
        // public Tooltip getTooltip() {
        // // if (((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList().contains(questId)) {
        // // int refreshTicks = ((PlayerAccessor) playerEntity).getPlayerQuestRefreshTimerList().get(((PlayerAccessor) playerEntity).getPlayerFinishedQuestIdList().indexOf(questId));
        // // if (refreshTicks != -1) {
        // // refreshTicks = refreshTicks / 20;
        // // String string;
        // // if (refreshTicks >= 3600) {
        // // string = String.format("%02d:%02d:%02d", refreshTicks / 3600, (refreshTicks % 3600) / 60, (refreshTicks % 60));
        // // } else {
        // // string = String.format("%02d:%02d", (refreshTicks % 3600) / 60, (refreshTicks % 60));
        // // }
        // // return Tooltip.of(Text.of(string));
        // // }
        // // }
        // return super.getTooltip();
        // }

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
            context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0, 166 + this.getTextureY(), this.getWidth(), this.getHeight(),
                    512, 512);
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
            this.setTooltip(Tooltip.of(Text.translatable("screen.villagerquests.declineButton")));
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 55, 166 + (this.isHovered() ? 15 : 0), this.getWidth(),
                    this.getHeight(), 512, 512);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

    }
}