package net.villagerquests.mixin;

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
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.gui.QuestScreenHandler;
import net.villagerquests.network.QuestClientPacket;

@Environment(EnvType.CLIENT)
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> implements MerchantAccessor {

    public MerchantScreenMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At(value = "TAIL"))
    protected void drawBackgroundMixin(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo info) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        RenderSystem.setShaderTexture(0, QuestScreenHandler.GUI_ICONS);
        if (this.isPointWithinBounds(275, 0, 20, 20, (double) mouseX, (double) mouseY)) {
            this.drawTexture(matrices, i + 275, j, 20, 0, 20, 20);
        } else
            this.drawTexture(matrices, i + 275, j, 0, 0, 20, 20);
    }

    // 275 and 0
    // 251 and 5
    @Inject(method = "mouseClicked", at = @At(value = "HEAD"), cancellable = true)
    private void mouseClickedMixin(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (this.isPointWithinBounds(275, 0, 20, 20, (double) mouseX, (double) mouseY)) {
            QuestClientPacket.writeC2SScreenPacket(((MerchantAccessor) this.client.player).getCurrentOfferer(), (int) this.client.mouse.getX(), (int) this.client.mouse.getY());
            info.cancel();
        }
    }

}
