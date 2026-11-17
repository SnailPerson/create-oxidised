package dev.mrmulle.createoxidized.forge;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.OxidationManager;
import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationManager.ScrapeResult;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateOxidizedCommon.MOD_ID)
public final class CreateOxidizedForge {
    @SuppressWarnings("removal")
    public CreateOxidizedForge() {
        OxidationItemData.install(new ForgeItemDataAdapter());
        ForgeCreativeTab.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfig.SPEC);
        CreateOxidizedCommon.installConfig(
            () -> ForgeConfig.OXIDATION_ENABLED.get(),
            () -> ForgeConfig.OXIDATION_SPEED_FACTOR.get(),
            () -> ForgeConfig.CLUTTER_REDUCER_3000.get()
        );
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        ItemStack stack = event.getItemStack();
        boolean changed = false;
        boolean damageTool = false;

        if (stack.is(Items.HONEYCOMB)) {
            changed = OxidationManager.wax(event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()));
            if (changed && !event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
        } else if (stack.getItem() instanceof AxeItem) {
            ScrapeResult result = OxidationManager.scrape(
                event.getLevel(),
                event.getPos(),
                event.getLevel().getBlockState(event.getPos())
            );
            changed = result != ScrapeResult.NONE;
            damageTool = changed;
        }

        if (!changed) {
            return;
        }

        event.getEntity().awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (damageTool) {
            stack.hurtAndBreak(1, event.getEntity(), player -> player.broadcastBreakEvent(event.getHand()));
        }
        event.getEntity().swing(event.getHand(), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}

