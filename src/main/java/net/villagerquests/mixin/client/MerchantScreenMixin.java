package net.villagerquests.mixin.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.gui.QuestScreenHandler;
import net.villagerquests.network.QuestClientPacket;

@Environment(EnvType.CLIENT)
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> implements MerchantAccessor {

    private final List<Integer> questIdList = new ArrayList<>();

    public MerchantScreenMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    protected void initMixin(CallbackInfo info) {
        this.questIdList.clear();
        this.questIdList.addAll(((MerchantAccessor) ((MerchantAccessor) this.client.player).getCurrentOfferer()).getQuestIdList());

        Iterator<Integer> iterator = this.questIdList.iterator();
        while (iterator.hasNext()) {
            int check = iterator.next();
            int questId = this.questIdList.get(this.questIdList.indexOf(check));
            if ((((PlayerAccessor) this.client.player).getPlayerQuestIdList().contains(questId)
                    && !((PlayerAccessor) this.client.player).isOriginalQuestGiver(((MerchantAccessor) this.client.player).getCurrentOfferer().getUuid(), questId))
                    || (((PlayerAccessor) this.client.player).getPlayerFinishedQuestIdList().contains(questId) && ((PlayerAccessor) this.client.player).getPlayerQuestRefreshTimerList()
                            .get(((PlayerAccessor) this.client.player).getPlayerFinishedQuestIdList().indexOf(questId)).equals(-1))) {
                iterator.remove();
            }

        }
    }

    @Inject(method = "drawBackground", at = @At(value = "TAIL"))
    protected void drawBackgroundMixin(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo info) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
        if (!questIdList.isEmpty())
            if (this.isPointWithinBounds(276, 0, 20, 20, (double) mouseX, (double) mouseY))
                MerchantScreenMixin.drawTexture(matrices, i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 20, 0, 20, 20, 512, 512);
            else
                MerchantScreenMixin.drawTexture(matrices, i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 0, 0, 20, 20, 512, 512);
        else
            MerchantScreenMixin.drawTexture(matrices, i + 276 + VillagerQuestsMain.CONFIG.xIconPosition, j + VillagerQuestsMain.CONFIG.yIconPosition, 80, 0, 20, 20, 512, 512);
    }

    // In window: 251 and 5
    @Inject(method = "mouseClicked", at = @At(value = "HEAD"), cancellable = true)
    private void mouseClickedMixin(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (this.isPointWithinBounds(276 + VillagerQuestsMain.CONFIG.xIconPosition, 0 + VillagerQuestsMain.CONFIG.yIconPosition, 20, 20, (double) mouseX, (double) mouseY) && !questIdList.isEmpty()) {
            QuestClientPacket.writeC2SScreenPacket(((MerchantAccessor) this.client.player).getCurrentOfferer(), (int) this.client.mouse.getX(), (int) this.client.mouse.getY());
            info.cancel();
        }
    }

}
