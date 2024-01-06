package net.villagerquests.mixin.ftb.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import net.villagerquests.init.ConfigInit;

@Mixin(NordColors.class)
public interface NordColorsMixin {

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 2369843))
    private static int colorMixinOne(int original) {
        if (ConfigInit.CONFIG.changeTeamGuiColor) {
            return 0;
        } else {
            return original;
        }
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 3028032))
    private static int colorMixinTwo(int original) {
        if (ConfigInit.CONFIG.changeTeamGuiColor) {
            return 5592405;
        } else {
            return original;
        }
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 3883602))
    private static int colorMixinThree(int original) {
        if (ConfigInit.CONFIG.changeTeamGuiColor) {
            return 9145227;
        } else {
            return original;
        }
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 4410462))
    private static int colorMixinFour(int original) {
        if (ConfigInit.CONFIG.changeTeamGuiColor) {
            return 6513507;
        } else {
            return original;
        }
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 5002858))
    private static int colorMixinFive(int original) {
        if (ConfigInit.CONFIG.changeTeamGuiColor) {
            return 14408667;
        } else {
            return original;
        }
    }

}
