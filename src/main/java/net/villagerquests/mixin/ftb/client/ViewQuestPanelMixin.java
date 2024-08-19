package net.villagerquests.mixin.ftb.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.villagerquests.screen.widget.ViewVillagerQuestButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(ViewQuestPanel.class)
public abstract class ViewQuestPanelMixin extends Panel {

    @Shadow(remap = false)
    private Quest quest;
    @Shadow(remap = false)
    private TextField titleField;

    public ViewQuestPanelMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "addWidgets", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", shift = Shift.AFTER, ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void addWidgetsMixin(CallbackInfo info, QuestObjectBase prev, boolean canEdit) {
        int iconSize = Math.min(16, this.titleField.height + 2);
        ViewVillagerQuestButton buttonVillagerQuest;
        this.add(buttonVillagerQuest = new ViewVillagerQuestButton((ViewQuestPanel) (Object) ViewQuestPanelMixin.this, quest, canEdit));
        buttonVillagerQuest.setPosAndSize(this.posX + iconSize + 8, 5, iconSize, iconSize);
    }

}
