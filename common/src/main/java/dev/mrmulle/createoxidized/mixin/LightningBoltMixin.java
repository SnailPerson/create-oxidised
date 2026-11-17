package dev.mrmulle.createoxidized.mixin;

import dev.mrmulle.createoxidized.OxidationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {
    @Inject(method = "clearCopperOnLightningStrike", at = @At("TAIL"))
    private static void createoxidized$cleanCreateCopper(Level level, BlockPos pos, CallbackInfo callback) {
        OxidationManager.lightningStrike(level, pos);
    }
}

