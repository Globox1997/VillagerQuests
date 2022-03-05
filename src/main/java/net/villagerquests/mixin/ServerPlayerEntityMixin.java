package net.villagerquests.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.network.QuestServerPacket;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    private boolean syncQuest = false;

    @Inject(method = "playerTick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;totalExperience:I", ordinal = 0, shift = At.Shift.BEFORE))
    public void playerTickMixin(CallbackInfo info) {
        if (this.syncQuest) {
            QuestServerPacket.writeS2CPlayerQuestDataPacket((ServerPlayerEntity) (Object) this);
            this.syncQuest = false;
        }
    }

    @Inject(method = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At(value = "TAIL"))
    public void copyFromMixinTwo(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
        this.syncQuest = true;
    }

    @Inject(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setWorld(Lnet/minecraft/server/world/ServerWorld;)V"))
    void teleportFix(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo info) {
        this.syncQuest = true;
    }

    @Nullable
    @Inject(method = "moveToWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;syncedFoodLevel:I", ordinal = 0))
    private void moveToWorldMixin(ServerWorld destination, CallbackInfoReturnable<Entity> info) {
        this.syncQuest = true;
    }

    @Inject(method = "playerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/LocationArrivalCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void playerTickLocationMixin(CallbackInfo info) {
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;
        if (serverPlayerEntity.age % 100 == 0) {
            List<List<Object>> travelIdList = ((PlayerAccessor) serverPlayerEntity).getPlayerTravelList();
            if (!travelIdList.isEmpty() && serverPlayerEntity.getWorld().getDimension() != null)
                for (int i = 0; i < travelIdList.size(); i++)
                    for (int u = 0; u < travelIdList.get(i).size() / 2; u++) {
                        String id = (String) travelIdList.get(i).get(u * 2);
                        if (!(boolean) travelIdList.get(i).get(u * 2 + 1))
                            if (serverPlayerEntity.getWorld().getStructureAccessor().method_41036().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY).containsId(new Identifier(id))) {
                                // public DynamicRegistryManager method_41036() {
                                // return this.world.getRegistryManager();
                                // }
                                if (serverPlayerEntity.getWorld().getStructureAccessor()
                                        .getStructureAt(serverPlayerEntity.getBlockPos(),
                                                serverPlayerEntity.getWorld().getStructureAccessor().method_41036().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY).get(new Identifier(id)))
                                        .hasChildren()) {
                                    travelIdList.get(i).set(u * 2 + 1, true);
                                    QuestServerPacket.writeS2CQuestTravelAdditionPacket(serverPlayerEntity, i, u * 2 + 1);
                                }
                            } else {
                                if (serverPlayerEntity.getWorld().getRegistryManager().get(Registry.BIOME_KEY).get(new Identifier(id)) != null
                                        && serverPlayerEntity.getWorld().getBiome(serverPlayerEntity.getBlockPos()).matchesId(new Identifier(id))) {
                                    travelIdList.get(i).set(u * 2 + 1, true);
                                    QuestServerPacket.writeS2CQuestTravelAdditionPacket(serverPlayerEntity, i, u * 2 + 1);
                                }
                            }
                    }
        }

    }

}
