package net.villagerquests.access;

import java.util.Date;
import java.util.HashMap;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import org.jetbrains.annotations.Nullable;

public interface TeamDataAccessor {

    public void setQuestStarted(long questId, @Nullable Date time);

    public Long2LongMap getStarted();

    public Long2LongMap getCompleted();

    public HashMap<Long, Long> getTimer();

}
