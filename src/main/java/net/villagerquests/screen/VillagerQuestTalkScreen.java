package net.villagerquests.screen;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.network.QuestClientPacket;
import net.villagerquests.screen.widget.DescriptionWidget;

@Environment(EnvType.CLIENT)
public class VillagerQuestTalkScreen extends Screen {

    private final MerchantEntity merchantEntity;
    private final List<Text> talkText;
    private final long questId;
    private DescriptionWidget descriptionWidget;
    private List<Text> revealText = new ArrayList<Text>();
    private int backgroundWidth = 276;
    private int backgroundHeight = 166;
    private int timer = 0;
    private int lineRevealCount = 0;
    private List<Text> words = new ArrayList<Text>();

    public VillagerQuestTalkScreen(MerchantEntity merchantEntity, long questId, List<Text> talkText) {
        super(NarratorManager.EMPTY);
        this.merchantEntity = merchantEntity;
        this.questId = questId;
        this.talkText = talkText;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            QuestClientPacket.writeC2STalkPacket(this.merchantEntity.getId(), this.questId);
            this.client.setScreen(null);
        }).dimensions(this.width / 2 - 25, 210, 50, 20).build());

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        this.descriptionWidget = this.addDrawableChild(new DescriptionWidget(i + 70, j, 200, 160, this.revealText, this.textRenderer));
    }

    // Todo:
    // When bold whole line is bold
    // When style in inner word, spaces will get removed

    @Override
    public void tick() {
        timer++;
        if (timer % 6 == 0) {
            if (talkText.size() > lineRevealCount) {
                if (!talkText.get(lineRevealCount).getString().equals("")) {
                    this.client.player.playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.VOICE, 1.0f, 0.7f + this.client.world.getRandom().nextFloat() * 0.6f);
                    ((MerchantAccessor) this.merchantEntity).setTalkTime(8);

                } else {
                    revealText.add(talkText.get(lineRevealCount));
                }
                if (this.words.isEmpty() && !talkText.get(lineRevealCount).getString().equals("")) {
                    if (talkText.get(lineRevealCount).copy().getSiblings().isEmpty()) {

                        String allWords = talkText.get(lineRevealCount).getString();
                        String[] specificWords = allWords.split(" ");
                        int oldIndex = 0;
                        for (int u = 0; u < specificWords.length; u++) {
                            int index = oldIndex = allWords.indexOf(specificWords[u], oldIndex);
                            if (index > 0) {
                                if (Character.isWhitespace(allWords.charAt(index - 1))) {
                                    for (int o = 1; o < allWords.length(); o++) {
                                        if (0 <= index - o && Character.isWhitespace(allWords.charAt(index - o))) {
                                            specificWords[u] = " " + specificWords[u];
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                            oldIndex += specificWords[u].length();
                            this.words.add(Text.of(specificWords[u]));
                        }
                    } else {
                        for (int i = 0; i < talkText.get(lineRevealCount).copy().getSiblings().size(); i++) {
                            if (talkText.get(lineRevealCount).copy().getSiblings().get(i).getStyle().isEmpty()) {

                                String allWords = talkText.get(lineRevealCount).copy().getSiblings().get(i).getString();
                                String[] specificWords = allWords.split(" ");
                                int oldIndex = 0;
                                for (int u = 0; u < specificWords.length; u++) {
                                    int index = oldIndex = allWords.indexOf(specificWords[u], oldIndex);
                                    if (index > 0 && Character.isWhitespace(allWords.charAt(index - 1))) {
                                        for (int o = 1; o < allWords.length(); o++) {
                                            if (0 <= index - o && Character.isWhitespace(allWords.charAt(index - o))) {
                                                specificWords[u] = " " + specificWords[u];
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                    oldIndex += specificWords[u].length();
                                    this.words.add(Text.of(specificWords[u]));
                                }
                            } else {
                                this.words.add(talkText.get(lineRevealCount).copy().getSiblings().get(i));
                            }
                        }
                    }
                }

                if (!this.words.isEmpty()) {
                    if (this.revealText.size() == 0 || this.lineRevealCount >= this.revealText.size()) {
                        this.revealText.add(this.words.get(0));
                    } else {
                        MutableText text = this.revealText.get(this.lineRevealCount).copy();
                        text.append(this.words.get(0));
                        this.revealText.set(this.lineRevealCount, text);
                    }
                    this.words.remove(0);
                }
                if (this.words.isEmpty()) {
                    this.descriptionWidget.setScrollY(this.descriptionWidget.getMaxScrollY());
                    this.lineRevealCount++;
                }
            }
        }
        super.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        InventoryScreen.drawEntity(context, this.width / 2 - this.backgroundWidth / 2, this.height / 2 + 70, 50, this.width / 2 - this.backgroundWidth / 2 - 120, this.height / 2 - 140,
                this.merchantEntity);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
