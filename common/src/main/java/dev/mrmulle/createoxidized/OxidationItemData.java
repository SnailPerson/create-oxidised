package dev.mrmulle.createoxidized;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
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

    public interface Adapter {
        Optional<StoredState> read(ItemStack stack);

        void write(ItemStack stack, StoredState state);
    }

    public record StoredState(OxidationStage stage, boolean waxed) {
    }
}
