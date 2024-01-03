package net.villagerquests.access;

import java.util.Date;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public interface TeamDataAccessor {

    public void setQuestStarted(long questId, @Nullable Date time);

    public Long2LongOpenHashMap getStarted();

    public Long2LongOpenHashMap getCompleted();

}
