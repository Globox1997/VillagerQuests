package net.villagerquests.mixin.ftb;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.villagerquests.access.QuestAccessor;

@Mixin(Quest.class)
public abstract class QuestMixin extends QuestObject implements QuestAccessor {

    private boolean villagerQuest;
    @Nullable
    private UUID villagerUuid;
    private int timer;

    @Shadow(remap = false)
    private boolean invisible;
    @Shadow(remap = false)
    private int invisibleUntilTasks;
    @Shadow(remap = false)
    @Mutable
    @Final
    private List<QuestObject> dependencies;
    @Shadow(remap = false)
    @Mutable
    @Final
    private List<Task> tasks;
    @Shadow(remap = false)
    private Tristate hideUntilDepsVisible;
    @Shadow(remap = false)
    private Chapter chapter;

    public QuestMixin(long id) {
        super(id);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void initMixin(long id, Chapter chapter, CallbackInfo info) {
        this.villagerQuest = false;
        this.villagerUuid = null;
        this.timer = 0;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void writeDataMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("villagerquest", this.villagerQuest);
        if (this.villagerUuid != null) {
            nbt.putUuid("villageruuid", this.villagerUuid);
        }
        nbt.putInt("Timer", this.timer);
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void readDataMixin(NbtCompound nbt, CallbackInfo info) {
        this.villagerQuest = nbt.getBoolean("villagerquest");
        if (nbt.contains("villageruuid")) {
            this.villagerUuid = nbt.getUuid("villageruuid");
        }
        this.timer = nbt.getInt("Timer");
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void writeNetDataMixin(PacketByteBuf buffer, CallbackInfo info) {
        buffer.writeBoolean(this.villagerQuest);
        if (this.villagerQuest) {
            buffer.writeUuid(this.villagerUuid);
        }
        buffer.writeInt(this.timer);
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void readNetDataMixin(PacketByteBuf buffer, CallbackInfo info) {
        this.villagerQuest = buffer.readBoolean();
        if (this.villagerQuest) {
            this.villagerUuid = buffer.readUuid();
        }
        this.timer = buffer.readInt();
    }

    @Environment(EnvType.CLIENT) // maybe have to remove client annotation
    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void fillConfigGroupMixin(ConfigGroup config, CallbackInfo info) {
        ConfigGroup villagerQuest = config.getOrCreateSubgroup("villager_quest");
        villagerQuest.addBool("villager_quest", this.villagerQuest, (v) -> {
            this.villagerQuest = v;
        }, false).setNameKey("ftbquests.quest.misc.villager_quest");
        villagerQuest.addString("villager_uuid", this.villagerUuid != null ? this.villagerUuid.toString() : "", v -> {
            try {
                v = UUID.fromString(v).toString();
                this.villagerUuid = UUID.fromString(v);
            } catch (IllegalArgumentException illegalArgumentException) {
                v = "";
                this.villagerUuid = null;
            }
        }, "").setNameKey("ftbquests.quest.misc.villager_uuid");
        villagerQuest.addInt("timer", this.timer, (v) -> {
            this.timer = v;
        }, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.quest.misc.timer");
    }

    @Inject(method = "isVisible", at = @At("RETURN"), cancellable = true, remap = false)
    private void isVisibleMixin(TeamData data, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValue() && this.villagerQuest && !data.isStarted(this)) {
            info.setReturnValue(false);
        }
    }

    @Override
    public boolean isVillagerQuest() {
        return this.villagerQuest;
    }

    @Override
    public void setVillagerQuest(boolean villagerQuest) {
        this.villagerQuest = villagerQuest;
    }

    @Nullable
    @Override
    public UUID getVillagerQuestUuid() {
        return this.villagerUuid;
    }

    @Override
    public void setVillagerQuestUuid(UUID uuid) {
        this.villagerUuid = uuid;
    }

    /*
     * NOTICE
     * 
     * Sadly FTB does not give out permissions so I had to redo this method. It is based on the isVisible method inside the Quest class. 
     * [Source]: https://github.com/FTBTeam/FTB-Quests/blob/main/common/src/main/java/dev/ftb/ mods/ftbquests/quest/Quest.java#L722
     * 
     */
    @Override
    public boolean isQuestVisible(TeamData data) {
        if (!data.isCompleted(this)) {
            if (invisible && invisibleUntilTasks == 0) {
                return false;
            }
            if (invisible) {
                int taskCount = 0;
                for (int i = 0; i < tasks.size(); i++) {
                    if (data.isCompleted(tasks.get(i))) {
                        taskCount++;
                    }
                    if (taskCount < invisibleUntilTasks) {
                        return false;
                    }
                }
            }
            if (hideUntilDepsVisible.get(chapter.hideQuestUntilDepsVisible()) && !data.areDependenciesComplete((Quest) (Object) this)) {
                return false;
            } else if (this.dependencies.size() > 0 && !this.dependencies.get(0).isVisible(data)) {
                return false;
            }

        }
        return true;
    }

    @Override
    public int getTimer() {
        return this.timer;
    }

}
