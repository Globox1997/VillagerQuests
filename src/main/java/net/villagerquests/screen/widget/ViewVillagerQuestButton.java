package net.villagerquests.screen.widget;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
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

import java.util.List;
import java.util.UUID;

public class ViewVillagerQuestButton extends SimpleTextButton {
    private final Quest quest;
    private final boolean canEdit;
    private boolean villagerQuest = false;

    public ViewVillagerQuestButton(ViewQuestPanel vieQuestPanel, Quest quest, boolean canEdit) {
        super(vieQuestPanel, Text.translatable("ftbquests.quest.misc.villager_quest"), ItemIcon.getItemIcon(Items.BOOK)); //(ViewQuestPanel) (Object) ViewQuestPanelMixin.this,
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
            c.setValue(villagerUuidString);
            EditStringConfigOverlay<String> overlay = new EditStringConfigOverlay<>(getGui(), c, accepted -> {
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
                    NetworkManager.sendToServer(EditObjectMessage.forQuestObject(this.quest));
                    if (uuid != null) {
                        QuestClientPacket.writeC2SUpdateMerchantQuestMark(uuid);
                    }
                    if (oldVillagerUuid != null && oldVillagerUuid != uuid) {
                        QuestClientPacket.writeC2SUpdateMerchantQuestMark(oldVillagerUuid);
                    }
                }
                this.openGui();
            }, Text.translatable("ftbquests.quest.misc.villager_uuid"));
            overlay.setWidth(203);
            overlay.setPos(this.getX() - 16, this.getY() - 50);
            getGui().pushModalPanel(overlay);
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
