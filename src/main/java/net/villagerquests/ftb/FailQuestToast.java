package net.villagerquests.ftb;

import dev.ftb.mods.ftbquests.client.gui.ToastQuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class FailQuestToast extends ToastQuestObject {

    public FailQuestToast(QuestObject questObject) {
        super(questObject);
    }

    @Override
    public boolean isImportant() {
        return false;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("ftbquests.quest.failed");
    }

}
