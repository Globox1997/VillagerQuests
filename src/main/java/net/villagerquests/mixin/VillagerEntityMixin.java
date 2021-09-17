package net.villagerquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.network.QuestServerPacket;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

    @Inject(method = "beginTradeWith", at = @At(value = "HEAD"))
    private void beginTradeWithMixin(PlayerEntity customer, CallbackInfo info) {
        ((MerchantAccessor) customer).setCurrentOfferer((MerchantEntity) (Object) this);
        QuestServerPacket.writeS2COffererPacket((ServerPlayerEntity) customer, (MerchantEntity) (Object) this);
    }

    // Lnet/minecraft/entity/passive/MerchantEntity;fillRecipesFromPool(Lnet/minecraft/village/TradeOfferList;[Lnet/minecraft/village/TradeOffers$Factory;I)V
    // net/minecraft/entity/passive/VillagerEntity.fillRecipesFromPool
    // (Lnet/minecraft/village/TradeOfferList;[Lnet/minecraft/village/TradeOffers$Factory;I)V

    // @Inject(method = "fillRecipes", at = @At(value = "INVOKE", target =
    // "Lnet/minecraft/entity/passive/VillagerEntity;fillRecipesFromPool(Lnet/minecraft/village/TradeOfferList;[Lnet/minecraft/village/TradeOffers$Factory;I)V", shift = Shift.AFTER), locals =
    // LocalCapture.CAPTURE_FAILSOFT)
    // protected void fillRecipesMixin(CallbackInfo info, VillagerData villagerData, Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap, TradeOffers.Factory factorys[], TradeOfferList tradeOfferList)
    // {
    // // // System.out.println(villagerData.getProfession().getId() + "::XXX::" + villagerData.getProfession().toString());

    // // VillagerData villagerData = this.getVillagerData();
    // // Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap =
    // // (Int2ObjectMap)TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
    // // if (int2ObjectMap != null && !int2ObjectMap.isEmpty()) {
    // // TradeOffers.Factory[] factorys =
    // // (TradeOffers.Factory[])int2ObjectMap.get(villagerData.getLevel());
    // // if (factorys != null) {
    // // TradeOfferList tradeOfferList = this.getOffers();
    // // this.fillRecipesFromPool(tradeOfferList, factorys, 2);
    // // }
    // // }

    // }

    // @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    // public void writeCustomDataToNbt(NbtCompound nbt) {
    // }

    // @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    // public void readCustomDataFromNbt(NbtCompound nbt) {
    // }

}
