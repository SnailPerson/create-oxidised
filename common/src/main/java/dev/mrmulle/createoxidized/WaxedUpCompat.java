package dev.mrmulle.createoxidized;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class WaxedUpCompat {
    // registry ids keep this compat safe when waxed up isnt installed
    private static final String WAXED_UP_NAMESPACE = "waxedup";
    private static final String COPPER_COATING = "copper_coating";
    private static final String OXIDISER = "oxidiser";

    private WaxedUpCompat() {
    }

    public static Treatment treatment(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!WAXED_UP_NAMESPACE.equals(id.getNamespace())) {
            return Treatment.NONE;
        }
        return switch (id.getPath()) {
            case COPPER_COATING -> Treatment.WAX;
            case OXIDISER -> Treatment.OXIDIZE;
            default -> Treatment.NONE;
        };
    }

    public static boolean canTreat(BlockState state, Treatment treatment) {
        if (!CreateOxidizedCommon.enabled() || !OxidationProperties.isOxidizable(state)) {
            return false;
        }
        return switch (treatment) {
            case WAX -> !state.getValue(OxidationProperties.WAXED);
            case OXIDIZE -> !state.getValue(OxidationProperties.WAXED)
                && state.getValue(OxidationProperties.STAGE) != OxidationStage.OXIDIZED;
            case NONE -> false;
        };
    }

    public static boolean treat(Level level, BlockPos pos, BlockState state, Treatment treatment) {
        return switch (treatment) {
            case WAX -> OxidationManager.wax(level, pos, state, false);
            case OXIDIZE -> OxidationManager.oxidize(level, pos, state);
            case NONE -> false;
        };
    }

    public static void playSprayEffect(ItemStack stack, Level level, BlockPos pos, Treatment treatment) {
        Item item = stack.getItem();
        // their effect method is optional too, reflection avoids linking their classes here
        try {
            item.getClass().getMethod("playEffect", Level.class, BlockPos.class).invoke(item, level, pos);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            if (treatment == Treatment.WAX) {
                level.levelEvent(3003, pos, 0);
            }
        }
    }

    public enum Treatment {
        NONE,
        WAX,
        OXIDIZE
    }
}
