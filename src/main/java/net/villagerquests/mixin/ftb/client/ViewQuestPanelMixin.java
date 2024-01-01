package net.villagerquests.mixin.ftb.client;

import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.villagerquests.access.MerchantAccessor;
import net.villagerquests.access.QuestAccessor;
import net.villagerquests.init.RenderInit;
import net.villagerquests.network.QuestClientPacket;

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
        ViewQuestPanelMixin.ViewVillagerQuestButton buttonVillagerQuest;
        this.add(buttonVillagerQuest = new ViewQuestPanelMixin.ViewVillagerQuestButton(quest, canEdit));
        buttonVillagerQuest.setPosAndSize(this.posX + iconSize + 8, 5, iconSize, iconSize);
    }

    private class ViewVillagerQuestButton extends SimpleTextButton {
        private final Quest quest;
        private final boolean canEdit;
        private boolean villagerQuest = false;

        public ViewVillagerQuestButton(Quest quest, boolean canEdit) {
            super((ViewQuestPanel) (Object) ViewQuestPanelMixin.this, Text.translatable("ftbquests.quest.misc.villager_quest"), ItemIcon.getItemIcon(Items.BOOK));
            this.quest = quest;
            this.canEdit = canEdit;
            if (this.quest != null) {
                this.villagerQuest = ((QuestAccessor) (Object) this.quest).isVillagerQuest() && ((QuestAccessor) (Object) this.quest).getVillagerQuestUuid() != null;
            }

        }

        @Override
        public void onClicked(MouseButton button) {
            if (this.canEdit) {
                this.playClickSound();
                String villagerUuidString;
                if (((QuestAccessor) (Object) this.quest).getVillagerQuestUuid() != null) {
                    villagerUuidString = ((QuestAccessor) (Object) this.quest).getVillagerQuestUuid().toString();
                } else {
                    villagerUuidString = "";
                }
                final UUID oldVillagerUuid = ((QuestAccessor) (Object) this.quest).getVillagerQuestUuid();

                if (button.isRight() && villagerUuidString.equals("")) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                        villagerUuidString = ((EntityHitResult) client.crosshairTarget).getEntity().getUuid().toString();
                    }
                }

                StringConfig c = new StringConfig(null);
                EditConfigFromStringScreen.open(c, villagerUuidString, "", Text.translatable("ftbquests.quest.misc.villager_uuid"), (accepted) -> {

                    if (accepted) {
                        String villagerUuid = (String) c.getValue();
                        UUID uuid = null;
                        try {
                            uuid = UUID.fromString(villagerUuid);
                            ((QuestAccessor) (Object) this.quest).setVillagerQuestUuid(uuid);
                            ((QuestAccessor) (Object) this.quest).setVillagerQuest(true);

                        } catch (IllegalArgumentException illegalArgumentException) {
                            ((QuestAccessor) (Object) this.quest).setVillagerQuest(false);
                            ((QuestAccessor) (Object) this.quest).setVillagerQuestUuid(null);
                            if (oldVillagerUuid != null) {
                                QuestClientPacket.writeC2SUpdateMerchantQuestMark(oldVillagerUuid);
                            }
                        }
                        (new EditObjectMessage(this.quest)).sendToServer();
                        if (uuid != null) {
                            QuestClientPacket.writeC2SUpdateMerchantQuestMark(uuid);
                        }
                        if (oldVillagerUuid != null && oldVillagerUuid != uuid) {
                            QuestClientPacket.writeC2SUpdateMerchantQuestMark(oldVillagerUuid);
                        }
                    }

                    this.openGui();
                });
            } else if (this.villagerQuest) {
                this.playClickSound();
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && client.player.getWorld() != null) {
                    List<MerchantEntity> list = client.player.getWorld().getEntitiesByClass(MerchantEntity.class, client.player.getBoundingBox().expand(16D), EntityPredicates.EXCEPT_SPECTATOR);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getUuid().equals(((QuestAccessor) (Object) this.quest).getVillagerQuestUuid())) {
                            ((MerchantAccessor) list.get(i)).setOffererGlow();
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public boolean isEnabled() {
            if (quest == null) {
                return false;
            }
            return true;
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (this.canEdit || this.villagerQuest) {
                super.addMouseOverText(list);
            }
        }

        @Override
        public CursorType getCursor() {
            if (!this.canEdit && !this.villagerQuest) {
                return CursorType.ARROW;
            }
            return super.getCursor();
        }

        @Override
        public void draw(DrawContext context, Theme theme, int x, int y, int w, int h) {
            if (this.villagerQuest) {
                context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, x, y, 504, 0, 8, 9, 512, 512);
            } else {
                context.drawTexture(RenderInit.VILLAGERQUEST_SCREEN_AND_ICONS, x, y, 496, 0, 8, 9, 512, 512);
            }
        }

    }

}
