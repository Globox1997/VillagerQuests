package net.villagerquests.accessor;

import java.util.List;

import net.minecraft.entity.passive.MerchantEntity;

public interface MerchantAccessor {

    public void clearQuestList();

    public MerchantEntity getCurrentOfferer();

    public List<Integer> getQuestIdList();

    public void setQuestIdList(List<Integer> idList);

    public void setCurrentOfferer(MerchantEntity merchantEntity);

}
