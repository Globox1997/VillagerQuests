package net.villagerquests.ftb;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.villagerquests.init.TaskInit;

public class VillagerTalkTask extends Task {
    private UUID villagerUuid;
    private String villagerName;
    private final List<String> villagerTalkText;

    public VillagerTalkTask(long id, Quest quest) {
        super(id, quest);
        villagerUuid = null;
        villagerName = "";
        villagerTalkText = new ArrayList<String>();
    }

    @Override
    public TaskType getType() {
        return TaskInit.VILLAGER_TALK;
    }

    @Override
    public void writeData(NbtCompound nbt) {
        super.writeData(nbt);
        if (villagerUuid != null) {
            nbt.putUuid("villageruuid", villagerUuid);
            nbt.putString("villagername", villagerName);

            if (!villagerTalkText.isEmpty()) {
                NbtList nbtList = new NbtList();
                Iterator<String> iterator = villagerTalkText.iterator();

                while (iterator.hasNext()) {
                    String value = iterator.next();
                    nbtList.add(NbtString.of(value));
                }

                nbt.put("villagertalktext", nbtList);
            }
        }
    }

    @Override
    public void readData(NbtCompound nbt) {
        super.readData(nbt);
        if (nbt.contains("villageruuid")) {
            villagerUuid = nbt.getUuid("villageruuid");
            villagerName = nbt.getString("villagername");

            villagerTalkText.clear();
            NbtList list = nbt.getList("villagertalktext", 8);

            for (int k = 0; k < list.size(); ++k) {
                villagerTalkText.add(list.getString(k));
            }
        }
    }

    @Override
    public void writeNetData(PacketByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeBoolean(villagerUuid != null);
        if (villagerUuid != null) {
            buffer.writeUuid(villagerUuid);
            buffer.writeString(villagerName);
            buffer.writeBoolean(!villagerTalkText.isEmpty());
            if (!villagerTalkText.isEmpty()) {
                NetUtils.writeStrings(buffer, villagerTalkText);
            }
        }
    }

    @Override
    public void readNetData(PacketByteBuf buffer) {
        super.readNetData(buffer);
        if (buffer.readBoolean()) {
            villagerUuid = buffer.readUuid();
            villagerName = buffer.readString();

            if (buffer.readBoolean()) {
                NetUtils.readStrings(buffer, villagerTalkText);
            } else {
                villagerTalkText.clear();
            }
        }

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("villager_uuid", villagerUuid != null ? villagerUuid.toString() : "", v -> {
            try {
                v = UUID.fromString(v).toString();
                villagerUuid = UUID.fromString(v);
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world != null && client.player != null) {
                    List<VillagerEntity> list = client.world.getEntitiesByClass(VillagerEntity.class, client.player.getBoundingBox().expand(16D), EntityPredicates.EXCEPT_SPECTATOR);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getUuid().equals(villagerUuid)) {
                            villagerName = list.get(i).getName().getString();
                            break;
                        }
                    }
                }

            } catch (IllegalArgumentException illegalArgumentException) {
                v = "";
                villagerUuid = null;
            }
        }, "").setNameKey("ftbquests.task.ftbquests.villager_talk.villager_uuid");
        StringConfig descType = new StringConfig();
        config.add("talk_text", new ListConfig<String, StringConfig>(descType) {
            public void onClicked(MouseButton button, ConfigCallback callback) {
                (new MultilineTextEditorScreen(Text.translatable("ftbquests.task.ftbquests.villager_talk.talk_text"), this, callback)).openGui();
            }
        }, villagerTalkText, (t) -> {
            villagerTalkText.clear();
            villagerTalkText.addAll(t);
        }, Collections.emptyList());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MutableText getAltTitle() {
        if (villagerUuid != null && !villagerName.equals("")) {
            return Text.literal(villagerName);
        }
        return Text.translatable("ftbquests.task.ftbquests.villager_talk.title");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Icon getAltIcon() {
        return ItemIcon.getItemIcon(Items.DIAMOND_BOOTS);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onButtonClicked(Button button, boolean canClick) {
    }

    @Environment(EnvType.CLIENT)
    public List<String> getTalkTextList() {
        return villagerTalkText;
    }

    @Nullable
    public UUID getVillagerUuid() {
        return this.villagerUuid;
    }

    public void talk(TeamData teamData, LivingEntity livingEntity) {
        if (!teamData.isCompleted(this) && villagerUuid != null && villagerUuid.equals(livingEntity.getUuid())) {
            teamData.addProgress(this, 1L);
        }
    }
}
