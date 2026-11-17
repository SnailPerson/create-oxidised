package dev.mrmulle.createoxidized.mixin;

import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationItemData.StoredState;
import dev.mrmulle.createoxidized.OxidationProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Unique
    private static final ThreadLocal<StoredState> CREATEOXIDIZED_PLACEMENT =
        new ThreadLocal<>();

    @Inject(method = "place", at = @At("HEAD"))
    private void createoxidized$captureItemState(
        BlockPlaceContext context,
        CallbackInfoReturnable<InteractionResult> callback
    ) {
        CREATEOXIDIZED_PLACEMENT.remove();
        OxidationItemData.read(context.getItemInHand())
            .ifPresent(CREATEOXIDIZED_PLACEMENT::set);
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void createoxidized$restorePlacedState(
        BlockPlaceContext context,
        CallbackInfoReturnable<InteractionResult> callback
    ) {
        StoredState stored = CREATEOXIDIZED_PLACEMENT.get();
        CREATEOXIDIZED_PLACEMENT.remove();
        if (stored == null || !callback.getReturnValue().consumesAction()) {
            return;
        }

        Level level = context.getLevel();
        if (level.isClientSide) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        BlockState placed = level.getBlockState(pos);
        if (!OxidationProperties.isOxidizable(placed)) {
            return;
        }

        setStoredState(level, pos, placed, stored);
        if (placed.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            DoubleBlockHalf half = placed.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState other = level.getBlockState(otherPos);
            if (other.is(placed.getBlock()) && OxidationProperties.isOxidizable(other)) {
                setStoredState(level, otherPos, other, stored);
            }
        }
    }

    @Unique
    private static void setStoredState(
        Level level,
        BlockPos pos,
        BlockState state,
        StoredState stored
    ) {
        level.setBlock(
            pos,
            state
                .setValue(OxidationProperties.STAGE, stored.stage())
                .setValue(OxidationProperties.WAXED, stored.waxed()),
            Block.UPDATE_ALL
        );
    }
}
