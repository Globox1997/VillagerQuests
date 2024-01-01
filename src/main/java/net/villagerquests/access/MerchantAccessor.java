package net.villagerquests.access;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.passive.MerchantEntity;

public interface MerchantAccessor {

    @Nullable
    public MerchantEntity getCurrentOfferer();

    public void setCurrentOfferer(MerchantEntity merchantEntity);

    public void setOffererGlow();

    public int getQuestMarkType();

    public void setQuestMarkType(int markType);

    public void setChangeableName(boolean changeableName);

    public boolean getChangeableName();

    public void setOffersTrades(boolean offersTrades);

    public boolean getOffersTrades();

    public void setTalkTime(int talkTime);

    public int getTalkTime();

}
