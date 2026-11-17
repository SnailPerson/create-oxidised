package dev.mrmulle.createoxidized.forge;

import java.util.Optional;

import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationItemData.StoredState;
import dev.mrmulle.createoxidized.OxidationStage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class ForgeItemDataAdapter implements OxidationItemData.Adapter {
    private static final String STAGE_KEY = "CreateOxidizedStage";
    private static final String WAXED_KEY = "CreateOxidizedWaxed";

    @Override
    public Optional<StoredState> read(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(STAGE_KEY)) {
            return Optional.empty();
        }
        return Optional.of(new StoredState(
            OxidationStage.fromSerializedName(tag.getString(STAGE_KEY)),
            tag.getBoolean(WAXED_KEY)
        ));
    }

    @Override
    public void write(ItemStack stack, StoredState state) {
        Component baseName = stack.getHoverName();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(STAGE_KEY, state.stage().getSerializedName());
        tag.putBoolean(WAXED_KEY, state.waxed());
        if (state.stage() != OxidationStage.UNAFFECTED || state.waxed()) {
            stack.setHoverName(variantName(baseName, state));
        }
    }

    private static Component variantName(Component baseName, StoredState state) {
        String key = "item.createoxidized.variant."
            + (state.waxed() ? "waxed." : "")
            + state.stage().getSerializedName();
        return Component.translatable(key, baseName)
            .withStyle(style -> style.withItalic(false));
    }
}
