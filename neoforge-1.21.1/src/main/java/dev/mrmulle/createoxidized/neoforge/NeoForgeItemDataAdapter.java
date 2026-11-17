package dev.mrmulle.createoxidized.neoforge;

import java.util.Optional;

import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationItemData.StoredState;
import dev.mrmulle.createoxidized.OxidationStage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class NeoForgeItemDataAdapter implements OxidationItemData.Adapter {
    private static final String STAGE_KEY = "CreateOxidizedStage";
    private static final String WAXED_KEY = "CreateOxidizedWaxed";

    @Override
    public Optional<StoredState> read(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return Optional.empty();
        }
        CompoundTag tag = data.copyTag();
        if (!tag.contains(STAGE_KEY)) {
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
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(STAGE_KEY, state.stage().getSerializedName());
            tag.putBoolean(WAXED_KEY, state.waxed());
        });
        if (state.stage() != OxidationStage.UNAFFECTED || state.waxed()) {
            stack.set(
                DataComponents.CUSTOM_NAME,
                variantName(baseName, state)
            );
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
