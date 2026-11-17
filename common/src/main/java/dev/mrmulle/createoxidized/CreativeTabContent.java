package dev.mrmulle.createoxidized;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class CreativeTabContent {
    private CreativeTabContent() {
    }

    public static ItemStack icon() {
        return new ItemStack(block("create:copper_casing"));
    }

    public static void populate(CreativeModeTab.Output output) {
        for (String id : OxidationProperties.ORDERED_BLOCK_IDS) {
            Block block = block(id);
            if (block == null || block.asItem().getDefaultInstance().isEmpty()) {
                continue;
            }

            if (CreateOxidizedCommon.clutterReducer3000()) {
                continue;
            }

            for (OxidationStage stage : OxidationStage.values()) {
                if (stage != OxidationStage.UNAFFECTED) {
                    add(output, block, stage, false);
                }
                add(output, block, stage, true);
            }
        }
    }

    private static void add(
        CreativeModeTab.Output output,
        Block block,
        OxidationStage stage,
        boolean waxed
    ) {
        ItemStack stack = new ItemStack(block);
        OxidationItemData.write(stack, stage, waxed);
        output.accept(stack);
    }

    private static Block block(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null || !BuiltInRegistries.BLOCK.containsKey(location)) {
            return null;
        }
        return BuiltInRegistries.BLOCK.get(location);
    }
}
