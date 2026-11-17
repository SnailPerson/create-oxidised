package dev.mrmulle.createoxidized.mixin;

import dev.mrmulle.createoxidized.OxidationManager;
import dev.mrmulle.createoxidized.OxidationProperties;
import dev.mrmulle.createoxidized.OxidationStage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void createoxidized$markActiveOxidationStatesRandomTicking(
        CallbackInfoReturnable<Boolean> callback
    ) {
        BlockState state = (BlockState) (Object) this;
        if (OxidationProperties.hasProperties(state)) {
            callback.setReturnValue(
                OxidationProperties.isOxidizable(state)
                    && !state.getValue(OxidationProperties.WAXED)
                    && state.getValue(OxidationProperties.STAGE) != OxidationStage.OXIDIZED
            );
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void createoxidized$oxidationRandomTick(
        ServerLevel level,
        BlockPos pos,
        RandomSource random,
        CallbackInfo callback
    ) {
        OxidationManager.randomTick(level, pos, (BlockState) (Object) this, random);
    }
}

