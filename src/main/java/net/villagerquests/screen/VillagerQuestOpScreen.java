package net.villagerquests.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.villagerquests.network.QuestClientPacket;

@Environment(EnvType.CLIENT)
public class VillagerQuestOpScreen extends Screen {

    private static final Text VILLAGER_NAME_TEXT = Text.translatable("screen.villagerquests.nameWidget");

    private final MerchantEntity merchantEntity;

    private TextFieldWidget villagerTextFieldWidget;
    private CheckboxWidget changeableNameWidget;
    private CheckboxWidget invincibilityWidget;
    private CheckboxWidget hasAiWidget;
    private CheckboxWidget offersTradesWidget;

    private boolean defaultChangeableName;
    private boolean defaultInvincibility;
    private boolean defaultOffersTrades;

    public VillagerQuestOpScreen(MerchantEntity merchantEntity, boolean defaultChangeableName, boolean defaultInvincibility, boolean defaultOffersTrades) {
        super(NarratorManager.EMPTY);
        this.merchantEntity = merchantEntity;
        this.defaultChangeableName = defaultChangeableName;
        this.defaultInvincibility = defaultInvincibility;
        this.defaultOffersTrades = defaultOffersTrades;
    }

    @Override
    protected void init() {
        this.villagerTextFieldWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 50, 300, 20, this.merchantEntity.getName());
        this.villagerTextFieldWidget.setMaxLength(128);
        this.villagerTextFieldWidget.setText(this.merchantEntity.getName().getString());
        this.addSelectableChild(this.villagerTextFieldWidget);

        this.changeableNameWidget = new CheckboxWidget(this.width / 2 - 152, 76, 20, 20, Text.translatable("screen.villagerquests.changeableNameWidget"), this.defaultChangeableName);
        this.addSelectableChild(this.changeableNameWidget);

        this.invincibilityWidget = new CheckboxWidget(this.width / 2 - 152, 102, 20, 20, Text.translatable("screen.villagerquests.invincibilityWidget"), this.defaultInvincibility);
        this.addSelectableChild(this.invincibilityWidget);

        this.hasAiWidget = new CheckboxWidget(this.width / 2 - 152, 128, 20, 20, Text.translatable("screen.villagerquests.hasAiWidget"), !this.merchantEntity.isAiDisabled());
        this.addSelectableChild(this.hasAiWidget);

        this.offersTradesWidget = new CheckboxWidget(this.width / 2 - 152, 154, 20, 20, Text.translatable("screen.villagerquests.offersTradesWidget"), this.defaultOffersTrades);
        this.addSelectableChild(this.offersTradesWidget);

        // this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.villagerquests.questsWidget"), button -> {
        // }).dimensions(this.width / 2 - 152, 180, 50, 20).build());
        // PackScreen

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            QuestClientPacket.writeC2SOpMerchantPacket(this.merchantEntity.getId(), this.villagerTextFieldWidget.getText(), this.changeableNameWidget.isChecked(),
                    this.invincibilityWidget.isChecked(), this.hasAiWidget.isChecked(), this.offersTradesWidget.isChecked());
            this.client.setScreen(null);
        }).dimensions(this.width / 2 - 75, 206, 150, 20).build());
        this.setInitialFocus(this.villagerTextFieldWidget);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.villagerTextFieldWidget.getText();
        this.init(client, width, height);
        this.villagerTextFieldWidget.setText(string);
    }

    @Override
    public void tick() {
        this.villagerTextFieldWidget.tick();

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawTextWithShadow(this.textRenderer, VILLAGER_NAME_TEXT, this.width / 2 - 153, 40, 0xA0A0A0);
        this.villagerTextFieldWidget.render(context, mouseX, mouseY, delta);
        this.changeableNameWidget.render(context, mouseX, mouseY, delta);
        this.invincibilityWidget.render(context, mouseX, mouseY, delta);
        this.hasAiWidget.render(context, mouseX, mouseY, delta);
        this.offersTradesWidget.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

}
