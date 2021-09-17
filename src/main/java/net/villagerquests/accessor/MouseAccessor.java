package net.villagerquests.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MouseAccessor {

    public void setMousePosition(int x, int y);
}
