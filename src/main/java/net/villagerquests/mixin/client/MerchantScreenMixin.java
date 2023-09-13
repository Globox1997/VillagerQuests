package net.villagerquests.mixin.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;

@Environment(EnvType.CLIENT)
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> implements MerchantAccessor, Tab {

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

    @Override
    public Class<?> getParentScreenClass() {
        return this.getClass();
    }

}
