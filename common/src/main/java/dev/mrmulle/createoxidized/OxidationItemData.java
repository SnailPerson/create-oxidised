package dev.mrmulle.createoxidized;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class OxidationItemData {
    private static Adapter adapter = new Adapter() {
        @Override
        public Optional<StoredState> read(ItemStack stack) {
            return Optional.empty();
        }

        @Override
        public void write(ItemStack stack, StoredState state) {
        }
    };

    private OxidationItemData() {
    }

    public static void install(Adapter platformAdapter) {
        adapter = platformAdapter;
    }

    public static Optional<StoredState> read(ItemStack stack) {
        return adapter.read(stack);
    }

    public static void write(ItemStack stack, OxidationStage stage, boolean waxed) {
        adapter.write(stack, new StoredState(stage, waxed));
    }

    public static void writeFromBlockState(ItemStack stack, BlockState state) {
        if (!OxidationProperties.isOxidizable(state)) {
            return;
        }
        write(
            stack,
            state.getValue(OxidationProperties.STAGE),
            state.getValue(OxidationProperties.WAXED)
        );
    }

    public static boolean restoreFromHeldItem(
        Player player,
        Level level,
        BlockPos pos,
        BlockState placedState
    ) {
        if (!OxidationProperties.isOxidizable(placedState)) {
            return false;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem blockItem)
                || blockItem.getBlock() != placedState.getBlock()) {
                continue;
            }

            Optional<StoredState> stored = read(stack);
            if (stored.isEmpty()) {
                continue;
            }

            StoredState value = stored.get();
            level.setBlock(
                pos,
                placedState
                    .setValue(OxidationProperties.STAGE, value.stage())
                    .setValue(OxidationProperties.WAXED, value.waxed()),
                Block.UPDATE_ALL
            );
            return true;
        }
        return false;
    }

    public interface Adapter {
        Optional<StoredState> read(ItemStack stack);

        void write(ItemStack stack, StoredState state);
    }

    public record StoredState(OxidationStage stage, boolean waxed) {
    }
}
