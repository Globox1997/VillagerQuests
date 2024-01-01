package net.villagerquests;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.villagerquests.init.RenderInit;
import net.villagerquests.network.QuestClientPacket;

@Environment(EnvType.CLIENT)
public class VillagerQuestsClient implements ClientModInitializer {

    public static final EntityModelLayer QUEST_LAYER = new EntityModelLayer(new Identifier("villagerquests:quest_layer"), "quest_layer");

    @Override
    public void onInitializeClient() {
        RenderInit.init();
        QuestClientPacket.init();
    }

}
