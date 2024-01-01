package net.villagerquests.mixin.ftb;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import dev.ftb.mods.ftbquests.quest.task.KillTask;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KillTask.class)
public class KillTaskMixin {

    private RegistryKey<World> dimension;
    private boolean ignoreDimension;
    private boolean location;
    private int x, y, z, radius;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void initMixin(long id, Quest quest, CallbackInfo info) {
        dimension = World.OVERWORLD;
        ignoreDimension = true;
        location = false;
        x = y = z = 0;
        radius = 1;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void writeDataMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("location", location);
        nbt.putString("dimension", dimension.getValue().toString());
        nbt.putBoolean("ignore_dimension", ignoreDimension);
        nbt.putIntArray("position", new int[] { x, y, z });
        nbt.putInt("radius", radius);
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void readDataMixin(NbtCompound nbt, CallbackInfo info) {
        location = nbt.getBoolean("location");
        dimension = RegistryKey.of(RegistryKeys.WORLD, new Identifier(nbt.getString("dimension")));
        ignoreDimension = nbt.getBoolean("ignore_dimension");

        int[] pos = nbt.getIntArray("position");

        if (pos.length == 3) {
            x = pos[0];
            y = pos[1];
            z = pos[2];
        }
        radius = nbt.getInt("radius");
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void writeNetDataMixin(PacketByteBuf buffer, CallbackInfo info) {
        buffer.writeBoolean(location);
        buffer.writeIdentifier(dimension.getValue());
        buffer.writeBoolean(ignoreDimension);
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
        buffer.writeVarInt(z);
        buffer.writeVarInt(radius);
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void readNetDataMixin(PacketByteBuf buffer, CallbackInfo info) {
        location = buffer.readBoolean();
        dimension = RegistryKey.of(RegistryKeys.WORLD, buffer.readIdentifier());
        ignoreDimension = buffer.readBoolean();
        x = buffer.readVarInt();
        y = buffer.readVarInt();
        z = buffer.readVarInt();
        radius = buffer.readVarInt();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void fillConfigGroupMixin(ConfigGroup config, CallbackInfo info) {
        config.addString("dim", dimension.getValue().toString(), v -> dimension = RegistryKey.of(RegistryKeys.WORLD, new Identifier(v)), "minecraft:overworld");
        config.addBool("ignore_dim", ignoreDimension, v -> ignoreDimension = v, true);
        config.addBool("location", location, v -> location = v, false);
        config.addInt("x", x, v -> x = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        config.addInt("y", y, v -> y = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        config.addInt("z", z, v -> z = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        config.addInt("radius", radius, v -> radius = v, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Inject(method = "kill", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/TeamData;addProgress(Ldev/ftb/mods/ftbquests/quest/task/Task;J)V"), cancellable = true, remap = false)
    private void killMixin(TeamData teamData, LivingEntity e, CallbackInfo info) {
        if (!ignoreDimension) {
            if (dimension != e.getWorld().getRegistryKey()) {
                info.cancel();
            } else if (location && MathHelper.sqrt((float) e.squaredDistanceTo(x, y, z)) > radius) {
                info.cancel();
            }
        } else if (location && MathHelper.sqrt((float) e.squaredDistanceTo(x, y, z)) > radius) {
            info.cancel();
        }
    }
}
