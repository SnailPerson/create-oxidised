package dev.mrmulle.createoxidized.mixin;

import java.util.List;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationProperties;
import dev.mrmulle.createoxidized.OxidationStage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @ModifyVariable(
        method = "registerDefaultState",
        at = @At("HEAD"),
        argsOnly = true
    )
    private BlockState createoxidized$initializeOxidationDefaults(BlockState state) {
        // minecraft wants every injected property to have a value in the default state
        if (!OxidationProperties.hasProperties(state)) {
            return state;
        }
        return state
            .setValue(OxidationProperties.STAGE, OxidationStage.UNAFFECTED)
            .setValue(OxidationProperties.WAXED, false);
    }

    @Inject(
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
        at = @At("RETURN")
    )
    private static void createoxidized$preserveOxidationInDrops(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        BlockEntity blockEntity,
        Entity breaker,
        ItemStack tool,
        CallbackInfoReturnable<List<ItemStack>> callback
    ) {
        // with clutter reducer on drops stay as plain Create items on purpose
        if (CreateOxidizedCommon.clutterReducer3000()
            || !OxidationProperties.isOxidizable(state)) {
            return;
        }
        OxidationStage stage = state.getValue(OxidationProperties.STAGE);
        boolean waxed = state.getValue(OxidationProperties.WAXED);
        if (stage == OxidationStage.UNAFFECTED && !waxed) {
            return;
        }
        for (ItemStack drop : callback.getReturnValue()) {
            if (drop.is(state.getBlock().asItem())) {
                OxidationItemData.write(drop, stage, waxed);
            }
        }
    }
}
